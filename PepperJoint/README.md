Pepper Joint
===============

### Feature
Android app <br/>
Interpolates Pepper's one joints to a target angle. <br/>
HeadYaw, HeadPitch, <br/>
LShoulderPitch, LShoulderRoll, LElbowYaw, LElbowRoll, LWristYaw, <br/>
RShoulderPitch, RShoulderRoll, RElbowYaw, RElbowRoll, RWristYaw, <br/>
HipRoll, HipPitch, KneePitch <br/>

This feature is similar to the motion in Choregraphe<br/>
<img src="https://raw.githubusercontent.com/ohwada/Pepper_Android/master/PepperJoint/docs/choregraphe_motion.png" width="400" /> <br/>

Note <br/>
This app is in development. <br/>

### API
ALMotion - Joint control <br/>
- Gets the names of all the joints : [NAOqi ALMotion#getBodyNames](http://doc.aldebaran.com/2-1/naoqi/motion/tools-general-api.html#ALMotionProxy::getBodyNames__ssCR)
- Get the minAngle, maxAngle : [NAOqi ALMotion#getLimits](http://doc.aldebaran.com/2-1/naoqi/motion/tools-general-api.html#ALMotionProxy::getLimits__ssCR)
- Gets the angles of the joints : [ALMotion#getAngles](http://doc.aldebaran.com/2-1/naoqi/motion/control-joint-api.html#ALMotionProxy::getAngles__AL::ALValueCR.bCR)
- Interpolates one joint to a target angle : [NAOqi ALMotion#angleInterpolationWithSpeed](http://doc.aldebaran.com/2-1/naoqi/motion/control-joint-api.html#ALMotionProxy::angleInterpolationWithSpeed__AL::ALValueCR.AL::ALValueCR.floatCR) <br/>

### Souce code
https://github.com/ohwada/Pepper_Android/tree/master/PepperJoint/PepperJoint <br/>

Require <br/>
java-naoqi-sdk-xxx-android.jar <br/>

### Blog (in Japanese)
http://pepper.ohwada.jp/archives/1120

### Screenshot
<img src="https://raw.githubusercontent.com/ohwada/Pepper_Android/master/PepperJoint/docs/screen.png" width="200" /> <br/>
