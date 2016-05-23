Smooth round image view based on https://github.com/vinc3m1/RoundedImageView

## How to use it

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

## How to use it in a listView

If you want to use the module inside a listView you can use it like this:

### with xml

alloy.js:
~~~js
var RV = require('com.miga.roundview');
~~~
xml:
~~~xml
<ListView id="listView" defaultItemTemplate="template">
    <Templates>
        <ItemTemplate name="template">
            <RoundView ns="RV" bindId="pic"/>
        </ItemTemplate>
    </Templates>
</ListView>
~~~

### pure js
alloy.js:
~~~js
var rv = require('com.miga.roundview');  
~~~
index.js
~~~js
var template = {
    properties: {
        backgroundColor: 'transparent',
        height: Ti.UI.SIZE
    },
    childTemplates: [{
        type: 'rv.RoundView',
        bindId: 'avatar',
        properties: {
            image:""
        }

    }]
};

var s = Ti.UI.createListSection();

var l = Ti.UI.createListView({
    templates: {
        'template': template
    },
    defaultItemTemplate: 'template',
    sections: [s]
});


var list = [];

for (var i = 0; i < 10; ++i) {

    list.push({
        avatar: {
            image: "/images/avatar.jpg",
            height: 40,
            width: 40,
            left: 0,
            borderwidth: 10,
            cornerradius: 100,
            bordercolor: "#ff0000"
        },
        activityText: {
            text: "bla",
            color:"#000"
        },
        properties: {
            height: 65,
            id: i
        }
    });
}
s.setItems(list);
$.index.add(l);
$.index.open();
~~~
