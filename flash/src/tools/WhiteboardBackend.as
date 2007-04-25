﻿package tools {	

	import flash.events.*;
	import flash.net.*;
	import flash.display.Sprite;
	import mx.controls.TextArea;
	import ink.Ink;
	import ink.InkStroke;
	import flash.display.Stage;
	import java.JavaIntegration;
	import flash.display.DisplayObject;
	import flash.display.LoaderInfo;
	import flash.filters.BevelFilter;
	import flash.display.SpreadMethod;
	import ink.InkUtils;
	import components.Whiteboard;
	import ink.InkCluster;
	import mx.containers.Canvas;
	import flash.geom.Rectangle;

	
	public class WhiteboardBackend extends Sprite {
		
		private var inkWell:Ink;
		private var debugText:TextArea;
		private var currInkStroke:InkStroke;

		// in case the numbers are too big, we should subtract them by this number
		// this number is set by the first sample that comes in that uses scientific notation
		private var xMinOffset:Number = -1;
		private var yMinOffset:Number = -1;
		
		private var theParent:Whiteboard = null;

		private var theStage:Stage;
		private var theRoot:DisplayObject;

		private var paramObj:Object;

		private var portNum:int = 8545; // default

		private var penTipCrossHair:Sprite = new Sprite();

		private var inkCanvas:Canvas;

		public function WhiteboardBackend(p:Whiteboard):void {
			trace("Whiteboard Started.");
			theParent = p;

			inkWell = new Ink();
			addChild(inkWell);
			currInkStroke = new InkStroke();
			
			penTipCrossHair.graphics.lineStyle(1, 0xDC3322);
			penTipCrossHair.graphics.drawCircle(-1, -1, 4);
			penTipCrossHair.x = -100;
			inkWell.addChild(penTipCrossHair);

			inkCanvas = theParent.inkCanvas;

			// notify java that we have started
	        theParent.javaBackend.send(ToolExplorerBackend.WHITEBOARD_MODE);
		}
		
		public function setDebugText(dbt:TextArea):void {
			debugText = dbt;
		}
		
		private function debugOut(msg:String):void {
			trace(msg);
			debugText.text = msg + "\n" + debugText;
		}

		public function recenter():void {
			inkWell.resetLocation();
		}
		
		public function setRoot(rt:DisplayObject):void {
			theRoot = rt;
		}
		
		public function setStage(stg:Stage):void {
			theStage = stg;
		}
		
		// now unused. It used to retrieve parameters from the host HTML page
		private function processParameters():void {
			try {
				var keyStr:String;
				var valueStr:String;
				paramObj = LoaderInfo(theRoot.loaderInfo).parameters;
				for (keyStr in paramObj) {
					valueStr = String(paramObj[keyStr]);
					trace(keyStr + ":\t" + valueStr);
					if (keyStr=="port") {
						portNum = parseInt(valueStr);
					}
				}
			} catch (error:Error) {
				trace(error);
			}
		}

        public function processMessage(msgText:String):void {
            // trace(msgText); // parse the text and assemble InkStrokes...
            var msg:XML = new XML(msgText);
            var msgName:String = msg.name();
            
            // this whole switching thing isn't the smartest...
            // perhaps we should get the node name of the XML?
            // because as it stands, <p would mess us up if it came
            // before penDownEvent
			if (msgName=="penDownEvent") {
				// start up a new stroke
   				currInkStroke = new InkStroke();
   				// add it to the stage
				inkWell.addChild(currInkStroke);
   			} else if (msgName=="p") {
				handleInk(msgText);
	   		} else if (msgName=="swatchColor") {
	   			// trace(msg..r + " " + msg..g + " " + msg..b);
	   			var intColor:int = 
	   				(parseInt(msg.@r) << 16) + 
	   				(parseInt(msg.@g) << 8) + 
	   				(parseInt(msg.@b));
	   			// trace(intColor+"");
	   			theParent.colorSwatch.selectedColor = intColor;
	   		} else if (msgName=="title") {
	   			theParent.titleLabel.text = msg.@value;
	   		}
        }

		
		private function handleInk(xmlTxt:String):void {
            var inkXML:XML = new XML(xmlTxt);
            // trace("XML: " + inkXML.toXMLString());
			// trace(inkXML.@x + " " + inkXML.@y + " " + inkXML.@f + " " + inkXML.@t + " " + inkXML.@p);

			var xVal:Number = 0;
			var xStr:String = inkXML.@x;
			xVal = InkUtils.getCoordinateValueFromString(xStr);
			// Figure out a minimum offset to reduce these large numbers!
			if (xMinOffset == -1) { // uninitialized
				xMinOffset = xVal;
			}
			xVal = xVal - xMinOffset;

			var yStr:String = inkXML.@y;
			var yVal:Number = 0;
			yVal = InkUtils.getCoordinateValueFromString(yStr);
			// Figure out a minimum offset to reduce these large numbers!
			if (yMinOffset == -1) { // uninitialized
				yMinOffset = yVal;
			}
			yVal = yVal - yMinOffset;

			// trace(xVal + ", " + yVal);

			var penUp:Boolean = inkXML.@p == "U";
			if (penUp) {
				inkWell.removeChild(currInkStroke);
				inkWell.addStroke(currInkStroke);
				currInkStroke.rerenderWithCurves();
				
				// penUps are a duplicate of the last regular sample
				// so, we do nothing, but possibly reposition it to the minimum 
				// (with some padding) after each stroke
				inkWell.recenterMostRecentCluster(new Rectangle(inkCanvas.x, inkCanvas.y, 
   																inkCanvas.width, inkCanvas.height));
			} else {
				// add samples to the current stroke
				currInkStroke.addPoint(xVal, yVal, parseFloat(inkXML.@f));

				penTipCrossHair.x = xVal;
				penTipCrossHair.y = yVal;
			}	
		}

		// make the ink easier to read by zooming in
		public function zoomIn():void {
			scaleX *= 1.25;
			scaleY *= 1.25;
		}
		public function resetZoom():void {
			scaleX = 1;
			scaleY = 1;
			inkWell.recenterMostRecentCluster(new Rectangle(inkCanvas.x, inkCanvas.y, 
   															inkCanvas.width, inkCanvas.height));
		}
		// make the ink smaller by zooming out
		public function zoomOut():void {
			scaleX *= .8;
			scaleY *= .8;
		}
		
		public function toggleDebugText():void {
			debugText.visible = !debugText.visible;
		}
        
        public function saveInk():void {
        	// send a message to java to save the current ink...
        }

        public function loadInk():void {
        	// send a message to java to load an ink file...
        }
        
 	}
}