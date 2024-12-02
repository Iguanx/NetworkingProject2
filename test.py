"""
Computer Networks Project 2
"""

import socket
import threading
from datetime import datetime, timedelta

def listen_to_server(client_socket):
    """
    Continuously listen for incoming messages from the server.
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
    delay = timedelta(seconds=s)
    endtime = datetime.now() + delay
    while datetime.now() < endtime:
        pass

#This function is mainly used when collecting port input
def get_positive_integer(Value):
        while True:
            user_input = input(Value + ": ")
            if user_input.isdigit():  # Check if the input is composed of digits
                number = int(user_input)
                if number > 0:  # Check if the number is positive
                    return number
            print("Invalid " + Value.lower() + ". Make sure to enter a positive integer.")
def main():
    connected=False
    inGroup=False
    username = ""
    firstConnection = True
    while(True):
        #Request user to input a command, all of which start with %
        print("Available commands:\n")
        print("\t%exit\n")
        if not connected:
            print("\t%connect\n")
        elif connected and not inGroup:
            print("\t%identify\n")
            print("\t%join\n")
        else:
            print("\t%identify\n")
            print("\t%users\n")
            print("\t%post\n")
            print("\t%message\n")
            print("\t%leave\n")
        cmnd = input("Enter a command: ")
        #Def a more efficient way to do this, but check to see if command is valid
        while (cmnd != "%connect" and cmnd != "%exit" and cmnd != "%identify" and cmnd != "%join" and cmnd != "%leave" and cmnd != "%users" and cmnd != "%post" and cmnd != "%message"):
                if (cmnd == ""):
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

if __name__ == "__main__":
    main()