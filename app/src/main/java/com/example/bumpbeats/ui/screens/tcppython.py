# %%
import socket
import serial
import threading

# Serial port settings for Arduino
SERIAL_PORT = "COM3"  # Update based on your device
BAUD_RATE = 9600

# TCP server settings
TCP_IP = "192.168.1.37"  # Laptop's IP address
TCP_PORT = 12345

def handle_client(client_socket, arduino_serial):
    try:
        while True:
            # Read data from Arduino
            if arduino_serial.in_waiting > 0:
                data = arduino_serial.readline().decode('utf-8').strip()
                print(f"Sending to client: {data}")
                client_socket.sendall((data + "\n").encode('utf-8'))  # Send to TCP client
    except Exception as e:
        print(f"Client connection error: {e}")
    finally:
        client_socket.close()

def main():
    # Connect to Arduino
    arduino_serial = serial.Serial(SERIAL_PORT, BAUD_RATE)
    print("Connected to Arduino.")

    # Start TCP server
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((TCP_IP, TCP_PORT))
    server_socket.listen(5)
    print(f"TCP Server listening on {TCP_IP}:{TCP_PORT}")

    while True:
        client_socket, addr = server_socket.accept()
        print(f"Client connected: {addr}")
        # Start a new thread for each client
        client_thread = threading.Thread(target=handle_client, args=(client_socket, arduino_serial))
        client_thread.start()

if __name__ == "__main__":
    main()



