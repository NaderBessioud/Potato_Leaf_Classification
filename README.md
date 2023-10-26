# Potato_Leaf_Classification
this project is compose from a web page and mobile app and both are using python FastAPI to access a neural network training model on a dataset of potato leafs with the use of tf_serving and microsoft azure function.
We used angular for the web page, java for the mobile app and python for the api.

## Training the Model
We used data from [kaggle](https://www.kaggle.com/datasets/arjuntejaswi/plant-village)
the Jupyter Notebook is located under training folder.

## Setup for Python
The python functions are locateds in api folder and training folder :
1) Install Python

2) Install Python packages:
pip3 install -r training/requirements.txt
pip3 install -r api/requirements.txt
3) Install Tensorflow Serving

## Setup for Angular
the angular project is under frontend folder :
cd frontend
npm install

## Running the API
### Using FastAPI & TF Serve
1) cd api
   
2) docker run -t --rm -p 8501:8501 -v C:/Users/ASUS/Desktop/workspace/PotatoDisease:/PotatoDisease tensorflow/serving --rest_api_port=8501 --model_config_file=/PotatoDisease/api/models.config

### Deploying the TF Lite on Azure
We uploaded the potatoes.h5 model to a storage account in microsoft azure and we deployeed a function that take image as parameter and return a json that contain the predicted class and the confidence using VS code.
The function is located under azure folder
