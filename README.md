How to Stream and How to Emulate a live encoder
###############################################

One format that most encoder support is plain RTP. So this project shows how RTP streams are received and
re-emitted as MPEG-DASH.

The sample server is not configurable in any way from the outside. The configuration is in the code and reflects the
prepared test content.

Please download
- the samples content from: http://com.mp4parser.s3.amazonaws.com/tos-mbr-in-one-file.mp4
- the release from:


Prerequisites:
##############

* java 8
* ffmpeg
* tos-mbr-in-one-file.mp4


Step 1 - Start the Server
#########################

    java -jar rtp2dash-1.0.jar


Step 2 - Start the stream
#########################

    ffmpeg -re -i tos-mbr-in-one-file.mp4 -c:v copy -c:a copy -map 0:0  -f rtp rtp://127.0.0.1:5000/  -c:v copy -c:a copy  -map 0:1  -f rtp rtp://127.0.0.1:5001/ -c:v copy -c:a copy  -map 0:2  -f rtp rtp://127.0.0.1:5002/  -c:v copy -c:a copy  -map 0:3  -f rtp rtp://127.0.0.1:5003/  -c:v copy -c:a copy  -map 0:4  -f rtp rtp://127.0.0.1:5004/  -c:v copy -c:a copy  -map 0:5  -f rtp rtp://127.0.0.1:5005/

Step 3 - Watch Tears of Steel
#############################

    Open your browser and go to: http://localhost:8080/index.html



ffmpeg -re -i tos-mbr-in-one-file.mp4 -c:v copy -c:a copy -map 0:0  -f rtp rtp://192.168.255.255:5000/  -c:v copy -c:a copy  -map 0:1  -f rtp rtp://192.168.255.255:5001/ -c:v copy -c:a copy  -map 0:2  -f rtp rtp://192.168.255.255:5002/  -c:v copy -c:a copy  -map 0:3  -f rtp rtp://192.168.255.255:5003/  -c:v copy -c:a copy  -map 0:4  -f rtp rtp://192.168.255.255:5004/  -c:v copy -c:a copy  -map 0:5  -f rtp rtp://192.168.255.255:5005/
