
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AvailableExpressionsInput {

	private Lock lock = new ReentrantLock();
//	private int a=0, b=0;
	
	private class Thread1 extends Thread {
		public void run() {
			int a=0,b=0;
			lock.lock();
		    int d = a+b;
		    lock.unlock();
		    
		    lock.lock();
		    int e = a+b;
		    lock.unlock();
	    }
	}
	
	private class Thread2 extends Thread {
		public void run() {
			int a=0,b=0;
			lock.lock();
		    int c = a+b;
		    lock.unlock();
		    
		    lock.lock();
		    a = 0;
		    lock.unlock();
	    }
	}
	
	
	public static void main(String[] args) {
		AvailableExpressionsInput ip = new AvailableExpressionsInput();
		
		Thread t1 = new Thread(ip.new Thread1());
        t1.start();
        
        Thread t2 = new Thread(ip.new Thread2());
        t2.start();
	}
}
