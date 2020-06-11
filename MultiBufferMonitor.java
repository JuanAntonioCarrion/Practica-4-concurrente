import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class MultiBufferMonitor {
	private ReentrantLock lock;
	private Condition notfull;
	private Condition notempty;
	private Queue<Integer> pedidos;
	
	private long[] productos;
	private int front, rear, count;
	private int n;
	
	public MultiBufferMonitor(int N) {
		pedidos = new LinkedList<>(); 
		n = N;
		lock = new ReentrantLock();
		notfull = lock.newCondition();
		notempty = lock.newCondition();
		productos = new long[N];
		front = 0;
		rear  = 0;
		count = 0;
	}
	
	public long[] extraer(int nelems, int id) {
		lock.lock();
		System.out.println("Consumidor " + id + " quiere llevarse " + nelems + " elementos.");

		if(count < nelems) {
			
			try { 
				pedidos.add(nelems);
				notfull.signalAll();
				notempty.await();
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
		int i = 0;
		long[] ret = new long[nelems];
		while(i < nelems) {
			ret[i] = productos[front];
			front = (front+1) % n;
			count--;
			i++;
		}
		
		
		System.out.println("Consumidor " + id + " se ha llevado " + nelems + " elementos.");
		System.out.println("Nelementos = " + count);
		//;
		lock.unlock();
		return ret;
	}
	
	public void almacenar(long[] prods, int id) {
		lock.lock();
		System.out.println("Productor " + id + " quiere almacenar " + prods.length + " elementos.");
		while((n-count) < prods.length) {
			try {
				notfull.await();
			} catch (InterruptedException e) { e.printStackTrace(); }
		}
		int i=0;
		while(i < prods.length) {
			productos[rear] = prods[i];
			rear = (rear+1) % n;
			count++;
			i++;
		}
		System.out.println("Productor " + id + " ha almacenado " + prods.length + " elementos.");
		System.out.println("Nelementos = " + count);

		if(!pedidos.isEmpty() && pedidos.peek() <= count) {
			pedidos.remove();
			notempty.signal();	
		}
		else{
			notfull.signal();
		}
		lock.unlock();
	}
}