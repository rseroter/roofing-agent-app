import os
from dotenv import load_dotenv

load_dotenv()

GOOGLE_CLOUD_PROJECT = os.getenv("GOOGLE_CLOUD_PROJECT")
TOPIC_ID = os.getenv("TOPIC_ID")
API_BASE_URL = os.getenv("API_BASE_URL")
