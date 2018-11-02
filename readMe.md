# Messenger Thing
Networking class project to build a chat messenger, UDP style. (no TCP or anything else fancy).


### Query Window
![Image of QueryWindow](https://github.com/xCito/InstantMessenger/blob/master/Screenshots/ssQueryWindow.png)

This is the first window that will appear. This window will query the user what port to run this application on and also to enter an optional nickname.


### Menu/Conversation Starter
![Image of Convo Starter](https://github.com/xCito/InstantMessenger/blob/master/Screenshots/ssConvoStarter.png)
This is the second window that will appear. This window displays the user's chosen nickname/username, current IP address on their network, and the port this Messenger Thing application is running on.
The user enter's the IP address and port number of another user (or their own) to start chatting with them.


### The Chat Window
![Image of Chat Window](https://github.com/xCito/InstantMessenger/blob/master/Screenshots/ssChatWindow.png)
This window will appear when the user attempts to start chatting with another user or when the user receives a message. 
If user1 is chatting with another user2 using this Messenger Thing application, they will know eachother's name. This is done right before that chatting begins. 

If user1 (the initiator) wants to send a message to IP: 123.123.123.123 and Port: 8080, user1 will enter their IP and Port into the Conversation Start window and hit the Start Convo button. In the background, user1's Chat Window will send a name request to that IP and Port, the, open the ChatWindow. If user2's Messenger Thing app is ready and listening for messages, it will automatic create a new ChatWindow on their end (which wont appear yet) and send a name response to the user1 (the initiator). user1's window will change the default name of the other user from IP | Port to the name received from user2's nameResponse.



*ScreenShot of that interaction*
![Image of Chat Window](https://github.com/xCito/InstantMessenger/blob/master/Screenshots/ssTwoInstancesTalking.png)
