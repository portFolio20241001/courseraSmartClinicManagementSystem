/*
  Import the base API URL from the config file
  Create a constant named PATIENT_API by appending '/patient' to the base URL


  Function  patientSignup
  Purpose  Register a new patient in the system

     Send a POST request to PATIENT_API with 
    - Headers  Content-Type set to 'application/json'
    - Body  JSON.stringify(data) where data includes patient details

    Convert the response to JSON and check for success
    - If response is not OK, throw an error with the message from the server

    Return an object with 
    - success  true or false
    - message  feedback from the server

    Use try-catch to handle network or API errors
    - Log errors and return a failure response with the error message


  Function  patientLogin
  Purpose  Authenticate a patient with email and password

     Send a POST request to `${PATIENT_API}/login`
    - Include appropriate headers and the login data in JSON format

    Return the raw fetch response to be handled where the function is called
    - The caller will check the response status and process the token or error


  Function  getPatientData
  Purpose  Fetch basic patient information using a token

     Send a GET request to `${PATIENT_API}/${token}`
    Parse the response and return the 'patient' object if response is OK
    If there's an error or the response is not OK, return null
    Catch and log any network or server errors


  Function  getPatientAppointments
  Purpose  Retrieve appointment data for a specific user (doctor or patient)

     Send a GET request to `${PATIENT_API}/${id}/${user}/${token}`
    - 'id' is the user’s ID, 'user' is either 'doctor' or 'patient', and 'token' is for auth

    Parse the response and return the 'appointments' array if successful
    If the response fails or an error occurs, return null
    Log any errors for debugging


  Function  filterAppointments
  Purpose  Retrieve filtered appointments based on condition and patient name

   Send a GET request to `${PATIENT_API}/filter/${condition}/${name}/${token}`
    - This allows filtering based on status or search criteria

   Parse the response if it's OK and return the data
   If the response fails, return an empty appointments array
   Use a try-catch to handle errors gracefully and notify the user
*/
