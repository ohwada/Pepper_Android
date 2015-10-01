Pepper Posture
===============

### Feature
Android app <br/>
Pepper go to the predefined postures. <br/>
Stand, StandInit, StandZero, Crouch<br/>

This feature is similar to the pose library in Choregraphe<br/>
<img src="https://raw.githubusercontent.com/ohwada/Pepper_Android/master/PepperPosture/docs/choregraphe_pose_library" width="200" /> <br/>

[NAO's Predefined postures](http://doc.aldebaran.com/2-1/family/robots/postures_robot.html#robot-postures)

### API
- get all predefined postures : [NAOqi ALRobotPosture#getPostureList](http://doc.aldebaran.com/2-1/naoqi/motion/alrobotposture-api.html#ALRobotPostureProxy::getPostureList)
- the robot go to the predefined posture : [NAOqi ALRobotPosture#goToPosture](http://doc.aldebaran.com/2-1/naoqi/motion/alrobotposture-api.html#ALRobotPostureProxy::goToPosture__ssC.floatC)

### Souce code
https://github.com/ohwada/Pepper_Android/tree/master/PepperPosture/PepperPosture <br/>

Require <br/>
java-naoqi-sdk-xxx-android.jar <br/>

### Screenshot
<img src="https://raw.githubusercontent.com/ohwada/Pepper_Android/master/PepperPosture/docs/screen.png" width="200" /> <br/>
