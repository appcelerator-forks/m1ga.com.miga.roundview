Smooth round image view based on https://github.com/vinc3m1/RoundedImageView

Create it in javascript:

~~~js
var round = require("com.miga.roundview");

roundview = round.createRoundView({
    top: 340,
    height: 100,
    width: 100,
    cornerradius: 100,
    borderwidth: 10,
    bordercolor: "#f00",
    image: "/images/head.jpg",
    backgroundColor: "transparent"
});
~~~

or Alloy xml

~~~xml
<RoundView id="abc" module="com.miga.roundview" cornerradius="100"  image="/images/head.jpg"  backgroundColor="transparent"/>
~~~
