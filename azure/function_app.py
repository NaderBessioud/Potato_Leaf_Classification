import azure.functions as func
import logging
from azure.storage.blob import BlobServiceClient, BlobClient, ContainerClient
import tensorflow as tf
import numpy as np
from PIL import  Image
import tempfile
import json

app = func.FunctionApp(http_auth_level=func.AuthLevel.ANONYMOUS)
CLASS_NAMES = ["Early Blight", "Late Blight", "Healthy"]
connection_string = "DefaultEndpointsProtocol=https;AccountName=potatodiseasecontainer;AccountKey=TtrGwPuWoQD0WzNwoiu8vDaTyYMTXAg7BKIRnrd1gwXK6vuwIv35d9cdYcFlNorrgqLcNX/xfFuC+AStyoyQng==;EndpointSuffix=core.windows.net"
model = None
def download_blob(blob_name, container_name):
    logger = logging.getLogger('my_function')
    # Connection to the container in the cloud
    blob_service_client = BlobServiceClient.from_connection_string(connection_string)
    container_client = blob_service_client.get_container_client(container_name)
    blob_client = container_client.get_blob_client(blob_name)

    # Creating a temporary HDF5 file
    with tempfile.NamedTemporaryFile(suffix=".h5", delete=False) as temp_file:
        temp_h5_file_path = temp_file.name
    
    # Downloading the model file into the temporary HDF5 file
    with open(temp_h5_file_path, "wb") as model_file:
        blob_data = blob_client.download_blob()
        model_data = blob_data.readall()
        model_file.write(model_data)
        logger.info("model donwload")
    return temp_h5_file_path

@app.route(route="predictdisease")
def predictdisease(req: func.HttpRequest) -> func.HttpResponse:
    loggerr = logging.getLogger('my_function')
    # Loading the model
    global model
    if model is  None :
        model = tf.keras.models.load_model(download_blob(
            "potato.h5",
            "models"
        ))
        loggerr.info("model load")

    # Retrieving the image named file from the HTTP request
    image = req.files['file']

    # Scaling and resizing the image 
    image = np.array(Image.open(image).convert("RGB").resize((256,256)))
    image = image/255
    img_array = tf.expand_dims(image ,0)

    # Predecting the class
    predictions = model.predict(img_array)

    # Retrieving the name of the class
    prediction = CLASS_NAMES[np.argmax(predictions[0])]

    # Retrieving the confidence and Rounding it to two decimal places.
    confidence = round(100 * (np.max(predictions[0])), 2)

    response_data = {
            "class_predicted": prediction,
            "confidence": confidence
        }

        # Serialize the dictionary to JSON
    response_json = json.dumps(response_data)

    return func.HttpResponse(response_json, mimetype="application/json", status_code=200)

  