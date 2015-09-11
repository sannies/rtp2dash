How to Stream and How to Emulate a live encoder
================================================

One format that most encoder support is plain RTP. So this project shows how RTP streams are received and
re-emitted as MPEG-DASH. It includes a stock Shaka Player to complete the showcase.

The sample server is not configurable in any way from the outside. The configuration is in the code and reflects the
prepared test content. While the stream is created it is a live stream with a dynamic manifest. Once the stream source
stopped emitting RTP packets the DASH stream will automatically become available as an on demand stream (even though
the profile is still the live profile).

Prerequisites:
---------------

* [java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [ffmpeg](https://www.ffmpeg.org/download.html)

additionally you can download a prebuilt version and the demo video:

* [Rtp2DASH Release 1.0](https://github.com/sannies/rtp2dash/releases/download/v1.0/rtp2dash-1.0.jar)
* [tos-mbr-in-one-file.mp4](http://com.mp4parser.s3.amazonaws.com/tos-mbr-in-one-file.mp4)


Step 1 - Start the Server
---------------

    java -jar rtp2dash-1.0.jar


Step 2 - Start the stream
---------------

    ffmpeg -re -i tos-mbr-in-one-file.mp4 -c:v copy -c:a copy -map 0:0  -f rtp rtp://127.0.0.1:5000/  -c:v copy -c:a copy  -map 0:1  -f rtp rtp://127.0.0.1:5001/ -c:v copy -c:a copy  -map 0:2  -f rtp rtp://127.0.0.1:5002/  -c:v copy -c:a copy  -map 0:3  -f rtp rtp://127.0.0.1:5003/  -c:v copy -c:a copy  -map 0:4  -f rtp rtp://127.0.0.1:5004/  -c:v copy -c:a copy  -map 0:5  -f rtp rtp://127.0.0.1:5005/

Step 3 - Watch Tears of Steel
---------------

1. Wait a moment (>10s so that at least 1 full video segment is available
2. Open your browser and go to: http://localhost:8080/index.html
3. Select "Tears of Steel" in the selection "Test manifest:"
4. Hit "Load Stream" and enjoy "Tears of Steel"



