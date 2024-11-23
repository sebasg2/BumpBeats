import socket
import serial
import threading
import time

def get_local_ip():
    """Retrieve the local IP address of the machine."""
    try:
        # Connect to an external server to get the local IP address
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.connect(("8.8.8.8", 80))  # Google's public DNS
            return s.getsockname()[0]
    except Exception as e:
        print(f"Error retrieving local IP: {e}")
        return "127.0.0.1"  # Fallback to localhost

def broadcast_ip(ip, port):
    """Broadcast the server's IP address."""
    broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    broadcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    message = f"{ip}:{port}"
    while True:
        broadcast_socket.sendto(message.encode("utf-8"), ("<broadcast>", 55555))
        time.sleep(2)  # Broadcast every 2 seconds

def start_server(ip, port):
    """Start the TCP server."""
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((ip, port))
    server_socket.listen(5)
    print(f"Server listening on {ip}:{port}")

    while True:
        client_socket, addr = server_socket.accept()
        print(f"Client connected: {addr}")
        threading.Thread(target=handle_client, args=(client_socket,)).start()

def handle_client(client_socket):
    """Handle communication with a single client."""
    try:
        while True:
            if arduino_serial.in_waiting > 0:
                data = arduino_serial.readline().decode("utf-8").strip()
                print(f"Sending: {data}")
                client_socket.sendall(f"{data}\n".encode("utf-8"))
    except Exception as e:
        print(f"Client connection error: {e}")
    finally:
        client_socket.close()

if __name__ == "__main__":
    ip = get_local_ip()  # Automatically detect the local IP address
    port = 12345

    # Set up serial connection to Arduino
    arduino_serial = serial.Serial("COM3", 9600)

    # Start broadcasting and the server
    threading.Thread(target=broadcast_ip, args=(ip, port)).start()
    start_server(ip, port)
