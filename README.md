Main: Serves as an entry point to the program and read the config file.
BodyFactory: a factory class that is responsible for returning pre defined bodies for codes 400, 404, 500, 501.
ContentTypeHelper: responsible for extracting the content type for files.
eRequestType: enum.
HttpRequest: responsible for parsing incoming Http Requests.
HttpResponse: responsible for sending responses back to the client.
HttpServer: the class that is the server itself. Defines the thread pool and manages requests and responses.
SenderUtils: defines methods that send responses to clients.

Design: 
HttpServer manages incoming requests and designates each client to it's own thread where his request is parsed.
Then we pass his request to the HttpResponse class, where the client request is passed to the HttpRequest class where it is parsed into a concrete request object with relevant params
and optional params are mapped. Next we check what is the request type and process it accordingly. Throughout the process errors are caught and handled as they should, insuring the server
doesn't crash.


