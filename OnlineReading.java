package OOP;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class PREDEF{
	public static int BOOKCURR = 3;
//	public static int USERCURR = 3;
}

//implement a onlinebook reading system
//1. allow users to read online simultaneously
//2. each user each time can read one book
//3. each book can be read by 3 readers Simultaneously
//4. user class will keep the info of book reading progress.


public class OnlineReading {
	public UserManager UM;
	public BookManager BM;
	public ReadOpr Opr;
	public OnlineReading(List<Book> b){
		UM = new UserManager();
		BM = new BookManager(b);
		Opr = new ReadOpr(BM,UM);
		UM.setOpr(Opr);
	}
	
	
}

class ReadOpr{
	ConcurrentHashMap<User,Book> reading;
	private BookManager BM;
	private UserManager UM;
	public ReadOpr(BookManager BM, UserManager UM){
		reading = new ConcurrentHashMap<>();
		this.BM = BM;
		this.UM = UM;
	}
	public boolean read(User u, Book b){
		if(UM.getStatus(u)==0){
			System.out.println("please login or register");
			return false;
		}
		if(reading.size()>PREDEF.BOOKCURR && !reading.containsKey(u)){
			System.out.println("now library is full, try later");
			return false;
		}
		b = BM.checkOut(b);
		if(b== null){
			System.out.println("please try again later or try other book");
			return false;
		}
		if(reading.containsKey(u)) finishreading(u);
		reading.put(u, b);
		return true;
	}
	public boolean finishreading(User u){
		Book b = reading.get(u);
		if(b==null) return false;
		int i = u.getProcess(b);
		u.setProcess(b, i+10);
		BM.checkIn(b);
		return true;
	}
	public boolean remove(User u){
		if(reading.containsKey(u)) reading.remove(u);
		return true;
	}
	
}


class User{
	private String username;
	private Map<Book,Integer> progress;
	private int status;  //1. normal. 0. revoke
	public User(String name){
		username = name;
		progress = new HashMap<>();
		status = 1;
	}
	public String getName(){return username;}
	public int getStatus(){return status;}
	public void renewMember(){this.status = 1;}
	public int getProcess(Book book){
		if(progress.containsKey(book)){
			return progress.get(book);
		}
		return 0;
	}
	public int setProcess(Book book,Integer page){
		if(page>=book.getPages()){
			page =book.getPages();
			System.out.println("Congratulations " + this.username + " on finishing "+book.getName());
			
		}
		System.out.println(this.username +" read book " + book.getName() +" to page:"+page);
		progress.put(book, page);
		
		return page;
		
	}
}

class Book{
	private String name;
	private Integer pages;
	public Book(String name, Integer pages){
		this.name = name;
		this.pages = pages;
	}
	public String getName(){return name;}
	public Integer getPages(){return pages;}
}

class UserManager{
	private ConcurrentHashMap<User,Integer> users; // 0. not login 1. login
	private ReadOpr opr;
	public UserManager(){
		users = new ConcurrentHashMap<>();
		this.opr = null;
		
	}
	void setOpr(ReadOpr opr){this.opr = opr;}
	public User addUser(User user){
		if(users.containsKey(user)){
			return null;
		}
		users.put(user, 0);
		return  user;
	}
	public Integer getStatus(User user){
		return users.get(user);
	}
	public User removeUser(User user){
		if(!users.containsKey(user)){
			return null;
		}
		users.remove(user);
		return  user;		
	}
	public boolean login(User user){
		if(user.getStatus()==1 && users.containsKey(user) && users.get(user)==0){
			users.put(user, 1);
			System.out.println(user.getName()+" login");
			return true;
		}
		return false;
	}
	public boolean logout(User user){
		if(users.containsKey(user) && users.get(user)==1){
			users.put(user, 0);
			opr.remove(user);
			System.out.println(user.getName()+" logout");
			return true;
		}
		return false;
	}
	
}


class BookManager{
	private ConcurrentHashMap<Book, Integer> shelf;
	public BookManager(List<Book> t){
		shelf = ShelfFactory.genShelf(t);
	}
	public void addBook(Book b){ shelf.put(b, 3);}
	public void removeBook(Book b){
		if(shelf.containsKey(b)) shelf.remove(b);
	}
	public synchronized Book checkIn(Book book){
		Integer val = shelf.get(book);
		if(val==null || val>3){
			System.out.println("Not our book, please check");
			return null;
			
		}
		shelf.put(book, val++);
		return book;
	}
	public synchronized Book checkOut(Book book){
		Integer val = shelf.get(book);
		if(val==null){
			System.out.println("No such book, please check");
			return null;			
		}
		else if(val<=0){
			System.out.println("all "+book.getName()+" checked out please wait");
			return null;			
		}
		else{
			shelf.put(book, val--);
			return book;
		}
	}
}

class ShelfFactory{
	public static ConcurrentHashMap<Book, Integer> genShelf(List<Book> t){
		ConcurrentHashMap<Book, Integer> shelf = new ConcurrentHashMap<>();
		for(Book x:t) shelf.put(x, 3);
		return shelf;
	}
}