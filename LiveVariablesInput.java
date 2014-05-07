
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LiveVariablesInput {

	private Lock lock = new ReentrantLock();
//	private int a=0, b=0;
	
	private class Thread1 extends Thread {
		public void run() {
			int a=0,b=0;
			lock.lock();
		    a = 0;
		    lock.unlock();
		    
		    lock.lock();
		    b = a;
		    lock.unlock();
	    }
	}
	
	private class Thread2 extends Thread {
		public void run() {
			int a=0,b=0;
			lock.lock();
		    a = 0;
		    lock.unlock();
		    
		    lock.lock();
		    a = 0;
		    lock.unlock();
	    }
	}
	
	
	public static void main(String[] args) {
		LiveVariablesInput ip = new LiveVariablesInput();
		
		Thread t1 = new Thread(ip.new Thread1());
        t1.start();
        
        Thread t2 = new Thread(ip.new Thread2());
        t2.start();
	}
}
