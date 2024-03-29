package java {
	
	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.SecurityErrorEvent;
	import flash.net.XMLSocket;

	
	/**
	 * Integrates with Java over a socket connection. 
	 * The Flash GUI acts like a client, with a Java server doing all the backend gruntwork.
	 */
	public class JavaIntegration {
		
		// communications with the Java backend
		private var sock:XMLSocket;

		// the Java backend is listening on this port
		private var port:int;
		
		// 
		public function JavaIntegration(portNum:int):void {
			port = portNum;
			startListening();
		}

		//
 		private function startListening():void {
			sock = new XMLSocket();
			configureListeners();
			
			// this should be gotten from the query parameter...
			// for now, we'll hard code it...
			sock.connect("localhost", port);
		}
		
		//
        private function configureListeners():void {
            sock.addEventListener(Event.CLOSE, closeHandler);
            sock.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
            sock.addEventListener(ProgressEvent.PROGRESS, progressHandler);
            sock.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
        }

		// add something to handle the data that comes from the Java backend...
		public function addMessageListener(msgListener:Function):void {
            sock.addEventListener(DataEvent.DATA, msgListener);
		}
        
		// From now on, users of this class need to handle their own Connect event
        public function addConnectListener(connectListener:Function):void {
            sock.addEventListener(Event.CONNECT, connectListener);
        }
        
        private function closeHandler(event:Event):void {
            trace("closeHandler: " + event);
        }

		private function debugOut(msg:String):void {
			send("flash client says: [" + msg + "]");
		}

        private function ioErrorHandler(event:IOErrorEvent):void {
            trace("ioErrorHandler: " + event);
        }

        private function progressHandler(event:ProgressEvent):void {
            trace("progressHandler loaded:" + event.bytesLoaded + " total: " + event.bytesTotal);
        }

        private function securityErrorHandler(event:SecurityErrorEvent):void {
            trace("securityErrorHandler: " + event);
        }

		/**
		 * Sends a line over to the Java backend.
		 */
		public function send(msg:String):void {
			if (sock==null || !sock.connected) {
				trace("Socket is null or not connected. Message ["+msg+"] was not sent.");
				return;
			}
			sock.send(msg + "\n");
		}
		
		public function sendWithArgs(msg:String, ...args):void {
			if (sock==null || !sock.connected) {
				trace("Socket is null or not connected. Message ["+msg+"] was not sent.");
				return;
			}
			
			// use ExternalCommunicationServer's command and argument delimiters...
			var messageWithArgs:String = "%%"+msg+"%%";
			for each (var arg:String in args) {
				messageWithArgs = messageWithArgs + "@_" + arg + "_@";
			}
			sock.send(messageWithArgs + "\n");
		}
		
	}
}