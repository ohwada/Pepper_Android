Pepper MoveTo
===============

### Feature
Android app <br/>
Pepper move to the given pose in the ground plane. <br/>

This app has nine arrow buttons. <br/>
When you touch the button, Pepper will move in the direction of the arrow. <br/>
When you touch the stop button in the middle, Pepper will stop. <br/>
[ Forward diagonally left ], [ Forward ], [ Forward diagonally right ] <br/>
[ Left side ], [ Stop ], [ Right side ] <br/>
[ Turn counter-clockwise ], [ Backward ], [ Turn clockwise ] <br/>

You can use three parameters in below. <br/>
Distance to move to forward and backward (m) : Initial value 0.5m <br/>
Distance to move to the left and right (m) : initial value 0.5m <br/>
Rotating angle (degrees): initial value 90 degrees <br/>

### PRECAUTIONS
Pepper might be falling in step or obstacle. <br/>
Please to stay near of the Pepper, you can stop immediately. <br/>

### API
ALMotion - Locomotion control<br/>
[NAOqi ALMotion#moveTo](http://doc.aldebaran.com/2-1/naoqi/motion/control-walk-api.html#ALMotionProxy::moveTo__floatCR.floatCR.floatCR) <br/>

### Souce code
https://github.com/ohwada/Pepper_Android/tree/master/PepperMoveTo/PepperMoveTo <br/>

Require <br/>
java-naoqi-sdk-xxx-android.jar <br/>

### Blog (in Japanese)
http://pepper.ohwada.jp/archives/1126

### Screenshot
<img src="https://raw.githubusercontent.com/ohwada/Pepper_Android/master/PepperMoveTo/docs/screen.png" width="200" /> <br/>
