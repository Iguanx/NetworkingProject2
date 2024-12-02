# NetworkingProject2
Computer Networking Project 2 

Project 2 Creating a Bulletin Board

Current commands worked on:
%connect (prompts for server host and port to connect to)
%join (for part 1 only one message board to join. Prompts for username and checks for originality)
%leave (removes current username from list of active usernames on server)
%exit (Leaves message board if already in it, and disconnects from server.)
%users (Lists all active users on message board)
%identify (gives info about client host and port)
%post (uploads a message to server, and is distrubuted to all active users in message board)
%message (retrieved message from server with given ID. Can only be run if an active member of the group the message is in)

TO RUN:
Run Java server first, then run Python client
Use %connect function, inputting 127.0.0.1 for host and 6789 for port
Try whatever functions you desire and let me know of issues you encounter
Additionally to see what multi-client experience is like, you may run test.py and Client.py at the same time.

***Be advised that sometimes after a new message from the server, you must press enter to get command input line back up. I am working on a fix for this.