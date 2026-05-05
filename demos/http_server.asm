.data
buf: .space 1024

msg_reset:  .asciiz "reset closed: "
msg_listen: .asciiz "\nListening on port 8080...\n"
msg_conn:   .asciiz "Client connected\n"
msg_fail:   .asciiz "Server setup failed\n"


# HTTP response format: PROTOCOL/VERSION STATUS_CODE REASON_PHASE\r\n Headers\r\n\r\n Contents
resp: .asciiz "HTTP/1.0 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>Hello from MIPS!</h1><p>This is your <i>HTTP</i> server!</p></body></html>\n"

.text
main:
    # Reset the IDE to a known state by closing all open files and sockets
    li   $v0, 1000
    syscall
    move $s7, $v0

    li   $v0, 4
    la   $a0, msg_reset
    syscall

    li   $v0, 1
    move $a0, $s7
    syscall

    # Create a TCP socket and listen on port 8080
    li   $v0, 1005 # SyscallTcpListen
    li   $a0, 8080 # Argument for port number
    syscall

    move $s0, $v0
    bltz $s0, failed

    li   $v0, 4
    la   $a0, msg_listen
    syscall

server_loop:
    # Run the accept syscall to accept a client connection
    # NOTE: as of right now this is a blocking syscall, and will not return until a client connects
    li   $v0, 1006 # SyscallTcpAccept
    move $a0, $s0
    syscall

    move $s1, $v0
    bltz $s1, server_loop # Restart loop if accept failed

    li   $v0, 4
    la   $a0, msg_conn # Success message
    syscall

    # Receive the HTTP request from the client
    # NOTE: we are currently not parsing the request and just responding with a static HTML page
    li   $v0, 1004 # SyscallTcpRecv
    move $a0, $s1 # Client handle (returned from accept)
    la   $a1, buf # Pointer to buffer for request data
    li   $a2, 1024 # Max number of bytes to receive
    syscall
    
    move $s2, $v0

    # Close the client connection if recv failed or client closed connection
    bltz $s2, close_client

    # Get the length of the response string to send back to the client
    la   $a1, resp
    jal  strlen          # length returned in $v0
    move $a2, $v0

    # Send the HTTP response back to the client with the response string and calculated length
    li   $v0, 1003 # SyscallTcpSend
    move $a0, $s1 # Connection handle
    la   $a1, resp # Pointer to response string
    syscall

close_client:
    # Close the client connection
    li   $v0, 1002 # SyscallTcpClose
    move $a0, $s1 # Client handle
    syscall

    # Go back to waiting for a new connection
    j    server_loop

# Failed message
failed:
    li   $v0, 4
    la   $a0, msg_fail
    syscall

    li   $v0, 10
    syscall

# Helper function to calculate the length of a null-terminated string
strlen:
    move $t0, $a1      # pointer
    li   $v0, 0        # length = 0

strlen_loop:
    lb   $t1, 0($t0)
    beq  $t1, $zero, strlen_done

    addi $t0, $t0, 1
    addi $v0, $v0, 1
    j    strlen_loop

strlen_done:
    jr   $ra