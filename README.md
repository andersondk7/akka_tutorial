# Overview
This project is broken down into a number of steps, each step demonstrates a concept of akka actors.  Each step is in its own package with a unit test to demonstrate the actor(s) in action.

With the exception of step 1 (which really does nothing...) provide a unit test to show compliance.  The accompanying project uses `org.scalatest.FunSpec`.

# Steps
## Step 1 -- basic actor
Create an actor that receives a message with some text and prints that text to the console.  The actor can just print unexpected messages to the console.
## Step 2 -- return to sender
Create an actor that receives a message with some text and echos back that text to the sender.  The actor can just print unexpected messages to the console.
## Step 3 -- simple supervision
Create an actor system that has a worker actor and a supervisor actor.  The worker actor receives a message with some text and echos back that text to the sender.  When the worker receives a message that it does not understand, it should throw an exception and the supervisor should catch that exception and restart the worker.
## Step 4 -- supervision that handles errors
Improve the actor system from step 3 to indicate to the original sender that the message was not accepted.
Have the supervisor catch the exception thrown by the worker and indicate to the sender that the message was not accepted.
## Step 5 -- simple actor state
Augment the actor system from step 4 to keep track of how many successful and failed messages are handled by the worker.  Include messages to get those counts back from the worker.
## Step 5a -- become
Augment the actor system from step 5 to keep track of how many sucessful and failed messages are handled by the worker. This time don't change mutable variables within the actor to track state, but change the behavior based on state.  (hint: use context.become to change the behavior of the actor based on changes in state, ie. successful or failed messages)
## Step 6 -- non-blocking 
Replace the action in the worker from step 5 to do a blocking call when a message is received.  For this exercise, when a message is received, write the message contents to a file (rather than a database or web service for example) and return the number of bytes written.  Also include a way to determine the total number of bytes written as well as then number of successful and failed messages handled. Do not write unknown messages to the file but do throw exception on an unknown message.  Remember that actors should not block!  You may want to consider creating a service to handle the blocking calls (ie. performs the call in a Future) and have the actor delegate to and respond from the service.
## Step6 a -- non-blocking w/become
Repeat step 6 but this time don't change mutable variables within the actor to track state, but change the behavior of the actor based on state.  This is similar to step 5a but the changes are in the supervisor (where the state is held)
## Step 7 
Repeat step 6a but this time let's be more functional.  Have the service always return a value (that is don't throw exceptions) by returning a Try[R] where R is the expected result.  We reserve thowing an exception to failures in actors rather than failures in services.
