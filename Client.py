"""
Computer Networks Project 2
"""

import socket
import threading
from datetime import datetime, timedelta

def listen_to_server(client_socket):
    """
    @brief  Continuously listen for incoming messages from the server.

    @param client_socket(socket): client socket
    """
    try:
        while True:
            # Receive data from the server
            message = client_socket.recv(1024).decode('utf-8')
            if not message:
                print("Connection closed by the server.")
                break
            print(f"\nServer: {message}")
    except Exception as e:
        print(f"Error while listening to the server: {e}")
    finally:
        client_socket.close()

def wait(s):
    """
    @brief  Adds delay

    @param s(int): seconds
    """
    delay = timedelta(seconds=s)
    endtime = datetime.now() + delay
    while datetime.now() < endtime:
        pass

#This function is mainly used when collecting port input
def get_positive_integer(Value):
        """
        @brief   checks user input for a positive integer

        @param  Value(int)  int to be checked

        @return integer
        """
        while True:
            user_input = input(Value + ": ")
            if user_input.isdigit():  # Check if the input is composed of digits
                number = int(user_input)
                if number > 0:  # Check if the number is positive
                    return number
            print("Invalid " + Value.lower() + ". Make sure to enter a positive integer.")

#Shows all the commands, for readability and the %help command
def show_available_commands(connected, inGroup):
    """
    @brief  Display available commands with a more user-friendly UI.
    """
    print("\n==================== AVAILABLE COMMANDS ====================\n")
    print("General Commands:")
    print("  %help       - Show all available commands")
    print("  %exit       - Exit the application")
    
    if not connected:
        print("\nConnection Commands:")
        print("  %connect    - Connect to the server")
    elif connected and not inGroup:
        print("\nUser Commands:")
        print("  %identify   - Identify the Server")
        print("  %join       - Join the public group")
    else:
        print("\nUser Commands:")
        print("  %identify   - Identify the Server")
        print("  %users      - List all users")
        print("  %post       - Post a message")
        print("  %message    - View a message")
        print("  %leave      - Leave the public group")

        print("\nGroup Commands:")
        print("  %groups     - List all private groups")
        print("  %groupjoin  - Join a specific group")
        print("  %grouppost  - Post a message to a group")
        print("  %groupusers - List users in the specific group")
        print("  %groupleave - Leave the specific group")
        print("  %groupmessage - View a message in the specific group")

    print("\n===========================================================")


#This returns a list of all the commands available so that we can loop through them
def get_available_commands(connected, inGroup):
    """
    @brief  Return a list of available commands based on connection and group status.
    """
    commands = ["%help", "%exit"]
    
    if not connected:
        commands.append("%connect")
    elif connected and not inGroup:
        commands.extend(["%identify", "%join"])
    else:
        commands.extend([
            "%identify", "%users", "%post", "%message", "%leave", 
            "%groups", "%groupjoin", "%grouppost", "%groupusers", 
            "%groupleave", "%groupmessage"
        ])
    
    return commands

def main():
    """
    @brief  Main function
    """
    connected=False
    inGroup=False
    username = ""
    firstConnection = True
    show_available_commands(connected, inGroup)
    while(True):
        #Request user to input a command, all of which start with %
        available_commands = get_available_commands(connected, inGroup)
        print("\n-- %help for list of available commands --")
        cmnd = input("Enter a command: ")
        #Def a more efficient way to do this, but check to see if command is valid
        while cmnd not in available_commands:
                if (cmnd == ""):
                    print("-- %help for list of available commands --")
                    cmnd = input("Enter a command: ")
                else:
                    cmnd = input("Command not recognized. Please enter a valid command: ")
        #Code to connect when connecting for first time
        if (cmnd == "%connect" and not connected):
            errorRaised = False
            while(not connected and not errorRaised):
                #Collect host and port from user
                host = input("Enter a host IP: ")
                port = get_positive_integer("Port")
                # Create a socket object
                client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                try:
                    client_socket.connect((host, port))
                    print(f"Connected to server at {host}:{port}")
                    connected = True
                    #Request username from user, and send to server
                    username = input("Enter a username: ")
                    client_socket.sendall(("%connect " + username + "\n").encode('utf-8'))
                    response = client_socket.recv(1024).decode('utf-8')
                    #The following line of code is for easy debugging purposes
                    #print(f"Server response: \n{response}")
                    while "User already exists:"in response:
                        print("Username " + username + " is taken, please try another")
                        username = input("Enter a username: ")
                        client_socket.sendall(("%connect " + username + "\n").encode('utf-8'))
                        response = client_socket.recv(1024).decode('utf-8')
                    #Check for correct after we've checked the username so we still get "Connected"
                    #Responses for if username is good, or if it already exists on the server
                    if "Message received: %connect" in response:
                        print("Connected to server with username " + username)
                except ConnectionError as e:
                    print(f"Connection error: {e}")
                    errorRaised = True
                except socket.gaierror as e:
                    print(f"Connection error: {e}")
                    errorRaised = True
        #Message for when user inputs connect command but is already connected
        elif (cmnd == "%connect" and connected):
            print("Already connected to a server. Use %exit if you wish to disconnect.")

        #Help command since we don't want every command showing all the time
        if (cmnd == "%help"):
            show_available_commands(connected, inGroup)
        #Command for when user wants to depart entirely from the program
        if (cmnd == "%exit"):
            #If actively in a group, leave the group and disconnect from server
            if connected and inGroup:
                print("Closing connection.")
                client_socket.sendall(("%leave " + username + "\n").encode('utf-8'))
                client_socket.close()
            #If connected but not in group, just disconnect from server
            elif connected and not inGroup:
                print("Closing connection.")
                client_socket.close()
            #Otherwise just stop program
            return 0
        #Print tuple with client socket host address and port
        if (cmnd == "%identify"):
            if connected:
                print(client_socket.getsockname())
            else:
                print("No socket opened yet.")
        #Command to join public message board
        if (cmnd == "%join"):
            #Reminder to connect to server before joining board
            if not connected:
                print("Must connect to server before joining a group!")
            #Reminder that join is not necessary if already in message board
            elif inGroup:
                print("You are already in the group. If desiring a different username, first use %leave")
            #Code to join message board
            else:
                #Request username from user, and send to server
                client_socket.sendall(("%join " + username + "\n").encode('utf-8'))
                if firstConnection:
                    response = client_socket.recv(1024).decode('utf-8')
                    if "User already" in response:
                        print (response)
                    elif "Message received:" in response:
                        print ("Successfully joined to group: " + username)
                    listener_thread = threading.Thread(target=listen_to_server, args=(client_socket,))
                    listener_thread.daemon = True  # Daemonize thread to close when main program exits
                    listener_thread.start()
                    firstConnection = False
                #The following line of code is for easy debugging purposes
                #print(f"Server response: \n{response}")
                #Responses for if username is good, or if it already exists on the server
                inGroup=True
                wait(1)

        #Command to leave the message board
        if (cmnd == "%leave"):
            #Reminder that you cannot leave message board if you're not in it in the first place
            if not inGroup:
                print("Must be in a group for %leave to work")
            #Code to leave message board
            else:
                #Tell server the active user is leaving (including username)
                client_socket.sendall(("%leave " + username + "\n").encode('utf-8'))
                inGroup=False
                wait(1)
                #Once again the following is for debugging:
                #print(f"Server response: \n{response}")
        #Command to list active users on message board
        if (cmnd == "%users"):
            #Two reminders that you need to be connacted and/or in the message board to see list of users
            if not connected:
                print("Must connect to server and join a group to check users in group")
            elif not inGroup:
                print("Must join a group to check users in current group")
            #Get and print list of users
            else:
                client_socket.sendall(("%users " + username + "\n").encode('utf-8'))
                wait(1)
        if cmnd == "%post":
            if not connected:
                print("Must connect to server and join a group to post messages")
            elif not inGroup:
                print("Must join a group to post messages")
            else:
                messageSubject = input("Enter message subject: ")
                while (messageSubject == ""):
                    messageSubject = input("Please enter a valid message subject: ")
                messageContent = input("Enter message: ")
                while (messageContent == ""):
                    messageContent = input("Please enter a valid message: ")
                time = datetime.now()
                messageTime = time.strftime("%x")
                client_socket.sendall(("%post||" + username + "||" + messageTime +"||" + messageSubject + "||" + messageContent + "\n").encode('utf-8'))
                wait(1)
        if cmnd == "%message":
            if not connected:
                print("Must connect to server and join a group to post messages")
            else:
                messageNum = get_positive_integer("Message ID")
                client_socket.sendall(("%message " + str(messageNum) + "\n").encode('utf-8'))
                wait(1)
        if cmnd == "%groups":
            if not connected:
                print("Must connect to server first.")
            else:
                client_socket.sendall(("%groups\n").encode('utf-8'))
                wait(1)
        if cmnd == "%groupjoin":
            if not connected:
                print("Must connect to server first.")
            else:
                group = input("Enter group ID or name: ")
                client_socket.sendall((f"%groupjoin {group}\n").encode('utf-8'))
                wait(1)
        if cmnd == "%groupusers":
            if not connected:
                print("Must connect to server first.")
            else:
                group = input("Enter group ID or name: ")
                client_socket.sendall((f"%groupusers {group}\n").encode('utf-8'))
                wait(1)
        if cmnd == "%grouppost":
            if not connected:
                print("Must connect to server first.")
            else:
                group = input("Enter group ID or name: ")
                messageSubject = input("Enter message subject: ")
                while not messageSubject:
                    messageSubject = input("Please enter a valid message subject: ")
                messageContent = input("Enter message content: ")
                while not messageContent:
                    messageContent = input("Please enter a valid message content: ")

                time = datetime.now()
                messageTime = time.strftime("%x %X")  # Include both date and time
                client_socket.sendall((f"%grouppost {group}||{username}||{messageTime}||{messageSubject}||{messageContent}\n").encode('utf-8'))
                wait(1)
        if cmnd == "%groupleave":
            if not connected:
                print("Must connect to server first.")
            else:
                group = input("Enter group ID or name: ")
                client_socket.sendall((f"%groupleave {group}\n").encode('utf-8'))
                wait(1)
        if cmnd == "%groupmessage":
            if not connected:
                print("Must connect to server first.")
            else:
                group = input("Enter group ID or name: ")
                messageID = get_positive_integer("Enter message ID")
                client_socket.sendall((f"%groupmessage {group} {messageID}\n").encode('utf-8'))
                wait(1)



if __name__ == "__main__":
    main()