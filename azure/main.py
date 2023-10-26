from azure.storage.blob import BlobServiceClient, BlobClient, ContainerClient
import tensorflow as tf
import numpy as np
from PIL import  Image


model = None
CLASS_NAMES = ["Early Blight", "Late Blight", "Healthy"]
connection_string = "DefaultEndpointsProtocol=https;AccountName=potatodiseasecontainer;AccountKey=TtrGwPuWoQD0WzNwoiu8vDaTyYMTXAg7BKIRnrd1gwXK6vuwIv35d9cdYcFlNorrgqLcNX/xfFuC+AStyoyQng==;EndpointSuffix=core.windows.net"


def download_blob(blob_name, container_name, destination_file):
    blob_service_client = BlobServiceClient.from_connection_string(connection_string)
    container_client = blob_service_client.get_container_client(container_name)
    blob_client = container_client.get_blob_client(blob_name)
    with open(destination_file, "wb") as file:
        blob_data = blob_client.download_blob()
        file.write(blob_data.readall())

def predict(request):
    global model
    if model is  None :
        download_blob(
            "potato.h5",
            "models",
            "/tmp/potato.h5"
        )

        model = tf.keras.models.load_model("/tmp/potato.h5")

    image = request.files['file']
    image = np.array(Image.open(image).convert("RGB").resize((256,256)))
    image = image/255
    img_array = tf.expand_dims(image ,0)

    predictions = model.predict(img_array)
    print(predictions)

    prediction = CLASS_NAMES[np.argmax(predictions[0])]
    confidence = round(100 * (np.max(predictions[0])), 2)

    return {"class_predicted":prediction, "confidence": confidence}
   