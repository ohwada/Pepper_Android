Pepper MoveToward
===============

### Feature
Android app <br/>
Pepper move at the given normalized velocity. <br/>

This app has nine arrow buttons. <br/>
While you touching the button, Pepper will move in the direction of the arrow. <br/>
When you release your finger from the button, Pepepr will stop.  <br/>
[ Forward diagonally left ], [ Forward ], [ Forward diagonally right ] <br/>
[ Left side ], [ Stop ], [ Right side ] <br/>
[ Turn counter-clockwise ], [ Backward ], [ Turn clockwise ] <br/>

You can use one parameters in below. <br/>
Moving speed (0 - 1.0) : The initial value of 0.1 <br/>

### PRECAUTIONS
Pepper might be falling in step or obstacle. <br/>
Please to stay near of the Pepper, you can stop immediately. <br/>
.
### API
ALMotion - Locomotion control<br/>
[NAOqi ALMotion#moveToward](http://doc.aldebaran.com/2-1/naoqi/motion/control-walk-api.html#ALMotionProxy::moveToward__floatCR.floatCR.floatCR) <br/>

### Souce code
https://github.com/ohwada/Pepper_Android/tree/master/PepperMoveToward/PepperMoveToward <br/>

Require <br/>
java-naoqi-sdk-xxx-android.jar <br/>

### Blog (in Japanese)
http://pepper.ohwada.jp/archives/1130

### Screenshot
<img src="https://raw.githubusercontent.com/ohwada/Pepper_Android/master/PepperMoveToward/docs/screen.png" width="200" /> <br/>
