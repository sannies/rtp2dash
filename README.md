How to Stream and How to Emulate a live encoder
###############################################

One format that most encoder support is plain RTP. If you don't have an encoder at hand I'd use ffmpeg for a test drive.
I prepared a multi-bit-rate and multi language file. Please download it from http://com.mp4parser.s3.amazonaws.com/tos-mbr-in-one-file.mp4

Step 1:

Start the stream:
ffmpeg -re -i tos-mbr-in-one-file.mp4 -c:v copy -c:a copy -map 0:0  -f rtp rtp://127.0.0.1:5000/  -c:v copy -c:a copy  -map 0:1  -f rtp rtp://127.0.0.1:5001/ -c:v copy -c:a copy  -map 0:2  -f rtp rtp://127.0.0.1:5002/  -c:v copy -c:a copy  -map 0:3  -f rtp rtp://127.0.0.1:5003/  -c:v copy -c:a copy  -map 0:4  -f rtp rtp://127.0.0.1:5004/  -c:v copy -c:a copy  -map 0:5  -f rtp rtp://127.0.0.1:5005/






ffmpeg -re -i tos-mbr-in-one-file.mp4 -c:v copy -c:a copy -map 0:0  -f rtp rtp://192.168.255.255:5000/  -c:v copy -c:a copy  -map 0:1  -f rtp rtp://192.168.255.255:5001/ -c:v copy -c:a copy  -map 0:2  -f rtp rtp://192.168.255.255:5002/  -c:v copy -c:a copy  -map 0:3  -f rtp rtp://192.168.255.255:5003/  -c:v copy -c:a copy  -map 0:4  -f rtp rtp://192.168.255.255:5004/  -c:v copy -c:a copy  -map 0:5  -f rtp rtp://192.168.255.255:5005/
