from google.adk.tools import ToolContext
from google.adk.events import Event
from google.cloud import pubsub_v1
import requests
from typing import Dict, Any, List
import json
import logging
from . import config

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def _log_history_to_pubsub(history_list: List[Event]) -> None:
    """Serializes a list of conversation events and publishes them to a Pub/Sub topic.

    Args:
        history_list: A list of `Event` objects representing the conversation history.
    """
    print(f"--- Tool: Found {len(history_list)} events in history ---")
    history_dicts = [event.model_dump() for event in history_list]

    for event_dict in history_dicts:
        if 'long_running_tool_ids' in event_dict and isinstance(event_dict['long_running_tool_ids'], set):
            event_dict['long_running_tool_ids'] = list(event_dict['long_running_tool_ids'])

    history_json_string = json.dumps(history_dicts, indent=2)
    
    publisher = pubsub_v1.PublisherClient()
    topic_path = publisher.topic_path(config.GOOGLE_CLOUD_PROJECT, config.TOPIC_ID)

    try:
        data = history_json_string.encode("utf-8")
        future = publisher.publish(topic_path, data)
        message_id = future.result()
        logging.info(f"Successfully published history to Pub/Sub topic {topic_path}, message ID: {message_id}")
    except Exception as e:
        logging.error(f"Error publishing history to Pub/Sub topic {topic_path}: {e}")

def _book_appointment_api_call(customer: str, slotid: str, address: str, services: List[str]) -> Dict[str, Any]:
    """Makes the HTTP POST request to the booking API to book an appointment.

    Constructs the request payload and sends a POST request to the booking service
    endpoint configured via the API_BASE_URL environment variable.

    Args:
        customer: The name of the customer.
        slotid: The ID of the appointment slot to book.
        address: The full address for the appointment.
        services: A list of services to be booked.

    Returns:
        A dictionary with the JSON response from the API upon success, or a
        dictionary with an 'error' key upon failure.
    """
    base_url = config.API_BASE_URL
    if not base_url:
        error_msg = "API_BASE_URL environment variable not set."
        logging.error(error_msg)
        return {"error": error_msg}

    endpoint = "/appointments/book"
    full_url = f"{base_url.rstrip('/')}{endpoint}"

    request_body = {
        "slotId": slotid,
        "customerName": customer,
        "fullAddress": address,
        "servicesToBook": services
    }

    headers = {
        "Content-Type": "application/json"
    }

    logging.info(f"Attempting to book appointment directly via HTTP POST to {full_url} for {customer} with slot ID {slotid}. Request: {request_body}")

    try:
        response = requests.post(full_url, json=request_body, headers=headers)
        response.raise_for_status()
        booking_response_data: Dict[str, Any] = response.json()
        logging.info(f"Successfully booked appointment via direct HTTP call. Status: {response.status_code}, Response: {booking_response_data}")
        return booking_response_data
    except requests.exceptions.HTTPError as http_err:
        logging.error(f"HTTP error occurred while booking appointment for {customer}: {http_err} - Response: {response.text}")
        return {"error": f"Failed to book appointment due to HTTP error: {http_err}", "details": response.text if response and hasattr(response, 'text') else "No response text available"}
    except Exception as e:
        logging.error(f"An unexpected error occurred while booking appointment for {customer} with slot ID {slotid}: {e}")
        return {"error": f"An unexpected error occurred: {str(e)}"}

def add_appointment(customer: str, slotid: str, address: str, services: List[str], tool_context: ToolContext) -> dict:
    """Adds a roofing appointment by calling the booking API and logs the conversation history.

    This function serves as a tool for the agent. It orchestrates the booking process by:
    1. Calling the internal `_book_appointment_api_call` function to make the actual API request.
    2. If the booking is successful, it retrieves the conversation history from the
       `tool_context` and logs it to a Pub/Sub topic via `_log_history_to_pubsub`.

    Args:
        customer: The name of the customer.
        slotid: The ID of the appointment slot to book.
        address: The full address for the appointment.
        services: A list of services to be booked for the appointment.
        tool_context: The context provided by the ADK, containing session information.

    Returns:
        A dictionary containing the booking confirmation details from the API,
        or an error dictionary if the booking failed.
    """
    booking_response = _book_appointment_api_call(customer, slotid, address, services)

    if "error" not in booking_response:
        history_list: List[Event] = tool_context._invocation_context.session.events # type: ignore
        _log_history_to_pubsub(history_list)
    
    return booking_response
