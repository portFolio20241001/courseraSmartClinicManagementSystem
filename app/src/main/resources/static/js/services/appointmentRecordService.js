/*
  Import the base API URL from the config file
  This helps keep URLs consistent and maintainable
import { API_BASE_URL } from "../config/config.js";

  Define a constant for the appointment endpoint by combining base URL and path
const APPOINTMENT_API = `${API_BASE_URL}/appointments`;


   Function: getAllAppointments
   Purpose: Fetch all appointments for a doctor based on a specific date and patient name

  Export an async function named getAllAppointments that takes 3 parameters:
    - date: the selected date to filter appointments
    - patientName: the name of the patient to search for
    - token: an authentication token used for secure API access

  Step 1: Use fetch() to send a GET request to the endpoint, including date, patientName, and token in the URL path
  Step 2: Check if the response is not OK; if so, throw an error
  Step 3: If successful, parse and return the response JSON



   Function: bookAppointment
   Purpose: Send a POST request to book a new appointment
 
  Export an async function named bookAppointment that takes:
    - appointment: an object containing appointment details (e.g., patient info, date/time)
    - token: authentication token for API access

  Step 1: Send a POST request using fetch()
    - Set method to "POST"
    - Include "Content-Type: application/json" in headers
    - Convert the appointment object to JSON and add it to the request body

  Step 2: Parse the response JSON
  Step 3: Return an object with:
    - success: true if response was OK, otherwise false
    - message: the message from the API or a fallback error

  Step 4: Add a try-catch block to handle network errors
    - In case of failure, log the error and return a default error message



   Function: updateAppointment
   Purpose: Update an existing appointment via a PUT request
 
  Export an async function named updateAppointment that takes:
    - appointment: the updated appointment data
    - token: authentication token

  Step 1: Use fetch() to send a PUT request to the same endpoint as booking
    - Set method to "PUT"
    - Include appropriate headers and stringify the appointment object

  Step 2: Parse the API response and return success status and message
  Step 3: Handle errors in a try-catch block, similar to the booking function
*/
