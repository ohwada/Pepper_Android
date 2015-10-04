Pepper Image Remote
===============

### Feature
Android app <br/>
Take a photo at the camera of Pepper, and display photo in Android phone. <br/>

### API
- Take a photo <br/>
[NAOqi ALVideoDevice#getImageRemote](http://doc.aldebaran.com/2-1/naoqi/vision/alvideodevice-api.html#ALVideoDeviceProxy::getImageRemote__ssCR) <br/>
This API take a photo and return photo with YUV format. <br/>

- Image conversion
Conversion from YUV to JPEG : [Android YuvImage#compressToJpeg](http://developer.android.com/reference/android/graphics/YuvImage.html#compressToJpeg(android.graphics.Rect, int, java.io.OutputStream)) <br/>
Conversion from JPEG to Bitmap : [Android BitmapFactory#decodeByteArray](http://developer.android.com/reference/android/graphics/BitmapFactory.html#decodeByteArray(byte[], int, int, android.graphics.BitmapFactory.Options)) <br/>

### Souce code
https://github.com/ohwada/Pepper_Android/tree/master/PepperImageRemote/ImageRemote <br/>

Require <br/>
java-naoqi-sdk-xxx-android.jar <br/>

### Blog (in Japanese)
http://pepper.ohwada.jp/archives/1144

### Screenshot
<img src="https://raw.githubusercontent.com/ohwada/Pepper_Android/master/PepperImageRemote/docs/screen.png" width="200" /> <br/>
