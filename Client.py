"""
Network Project 2
"""
import socket

#This function is mainly used when collecting port input
def get_positive_integer(Value):
        while True:
            user_input = input(Value + ": ")
            if user_input.isdigit():  # Check if the input is composed of digits
                number = int(user_input)
                if number > 0:  # Check if the number is positive
                    return number
            print("Invalid " + Value.lower() + "Make sure to enter a positive integer.")

def main():
    connected=False
    inGroup=False
    username = ""
    while(True):
        #Request user to input a command, all of which start with %
        cmnd = input("Enter a command: ")
        #Def a more efficient way to do this, but check to see if command is valid
        while (cmnd != "%connect" and cmnd != "%exit" and cmnd != "%identify" and cmnd != "%join" and cmnd != "%leave" and cmnd != "%users"):
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
            elif not inGroup:
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
                username = input("Enter a username: ")
                client_socket.sendall(("%join " + username + "\n").encode('utf-8'))
                response = client_socket.recv(1024).decode('utf-8')
                #The following line of code is for easy debugging purposes
                #print(f"Server response: \n{response}")
                #Responses for if username is good, or if it already exists on the server
                if "Message received: %join" in response:
                    print("Successfully joined group")
                    inGroup=True
                if "User already exists:"in response:
                    print("Username taken, please try another")
        #Command to leave the message board
        if (cmnd == "%leave"):
            #Reminder that you cannot leave message board if you're not in it in the first place
            if not inGroup:
                print("Must be in a group for %leave to work")
            #Code to leave message board
            else:
                #Tell server the active user is leaving (including username)
                client_socket.sendall(("%leave " + username + "\n").encode('utf-8'))
                response = client_socket.recv(1024).decode('utf-8')
                #Once again the following is for debugging:
                #print(f"Server response: \n{response}")
                #Responses for if user has left, or if server had any issues finding the user to disconnect
                if "Message received: %leave" in response:
                    print("Successfully left group")
                    inGroup=False
                if "Could not find user"in response:
                    print("Could not find user")
        #Command to list active users on message board
        if (cmnd == "%users"):
            #Two reminders that you need to be connacted and/or in the message board to see list of users
            if not connected:
                print("Must connect to server and join a group to check users in group")
            elif not inGroup:
                print("Must join a group to check users in current group")
            #Get and print list of users
            else:
                client_socket.sendall(("%users\n").encode('utf-8'))
                response = client_socket.recv(1024).decode('utf-8')
                print(response)
if __name__ == "__main__":
    main()