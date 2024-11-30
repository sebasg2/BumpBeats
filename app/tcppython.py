import serial
import socket
import threading
import time

def get_local_ip():
    """Retrieve the local IP address of the machine."""
    try:
        # Connect to an external server to get the local IP address
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.connect(("8.8.8.8", 80))  # Google's public DNS
            return s.getsockname()[0]
    except:
        return "127.0.0.1"  # Fallback to localhost

def broadcast_ip(ip, port):
    """Broadcast the server's IP and port."""
    broadcast_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    broadcast_socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    message = f"{ip}:{port}"
    while True:
        broadcast_socket.sendto(message.encode("utf-8"), ("<broadcast>", 55555))
        time.sleep(2)  # Broadcast every 2 seconds

def start_server(ip, port, arduino_serial):
    """Start a TCP server to send data to the phone."""
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((ip, port))
    server_socket.listen(5)
    print(f"Server is listening on {ip}:{port}")

    while True:
        client_socket, addr = server_socket.accept()
        print(f"Client connected: {addr}")
        threading.Thread(target=handle_client, args=(client_socket, arduino_serial)).start()

def handle_client(client_socket, arduino_serial):
    """Handle the connection with the phone."""
    try:
        while True:
            if arduino_serial.in_waiting > 0:
                data = arduino_serial.readline().decode("utf-8").strip()
                print(f"Sending to client: {data}")
                client_socket.sendall(f"{data}\n".encode("utf-8"))  # Send data to the phone
    except Exception as e:
        print(f"Client error: {e}")
    finally:
        client_socket.close()

if __name__ == "__main__":
    # Replace with your Arduino's port
    arduino_port = "COM13"
    baud_rate = 9600

    # Initialize serial connection to Arduino
    arduino_serial = serial.Serial(arduino_port, baud_rate)
    print("Connected to Arduino.")

    # Get local IP address
    local_ip = get_local_ip()
    tcp_port = 12345  # Port used for TCP communication

    # Start broadcasting IP and port
    threading.Thread(target=broadcast_ip, args=(local_ip, tcp_port), daemon=True).start()

    # Start TCP server to send data to the phone
    start_server(local_ip, tcp_port, arduino_serial)
