package OOP;
import java.util.*;
public class OnlineChat {
	public void execute(){
		ChatUserManager CUM = ChatUserManager.getInstance();
		ChatUser[] users = new ChatUser[5];
		for(int i=0;i<5;i++){
			users[i] = new ChatUser(i,"user"+String.valueOf(i));
			
		}
		for(int i=0;i<4;i++){
			CUM.addUser(users[i]);
		}
		for(int i=0;i<5;i++){
			int ret = CUM.Login(i);
			if(ret == 404){
				System.out.println("no such user");
			}
			else if(ret == 1){
				System.out.println("welcome");
			}
		}
//		CUM.addUser(users[4]);
//		CUM.startPrivateConvertation(users[1], users[2]);
//		CUM.startGroupConvertation(users[1],Arrays.asList(users));
//		CUM.sendMessage(users[1], new Message(users[1],"hello"), 1);
//		CUM.sendMessage(users[2], new Message(users[2],"welcome"), 1);
//		CUM.sendMessage(users[3], new Message(users[3],"welcome 3"), 1);
//		CUM.sendMessage(users[4], new Message(users[4],"welcome"), 2);
//		CUM.sendMessage(users[1], new Message(users[1],"welcome"), 2);
//		CUM.sendMessage(users[3], new Message(users[3],"welcome"), 2);
//		CUM.printMessage(users[1], 1);
//		System.out.println("*************");
//		CUM.printMessage(users[2], 2);
		for(int i=0;i<4;i++){
			users[i].addFriend(users[4]);
		}
		CUM.printFriendList(users[4]);
		
		
		
		
	}

	
}

//online chat system
//1. can add friend
//2. login, logoff
//3. chat(private & group)

class COUNT{
	public static int count = 0;
}

class ChatUserManager implements LogOpr{
	private static ChatUserManager instance = new ChatUserManager();
	
	private Map<Integer,ChatUser> usersById;
	private Map<Integer,ChatUser> onlineUsers;
	private Map<Integer, Conversation> chats;
	private Map<Integer, Set<Integer>> contacts;
	private ChatUserManager(){
		usersById = new HashMap<>();
		onlineUsers= new HashMap<>();
		chats = new HashMap<>();
		contacts = new HashMap<>();
	}
	public static ChatUserManager getInstance(){
		return instance;
	}
	public void addUser(ChatUser u){
		usersById.put(u.getId(), u);
	}
	private int preCheckConversation(ChatUser u, Integer i){
		if(!isLogin(u.getId())) return 128;
		Conversation c = chats.get(i);
		if(c==null) return 404;
		Set<ChatUser> users = c.getUserList();
		if(!users.contains(u)) return 80;
		return 1;
	}
	public int sendMessage(ChatUser u, Message m, Integer i){
		int ret = preCheckConversation(u,i);
		if(ret!=1) return ret;
		Conversation c = chats.get(i);
		c.addMessage(m);
		return ret;

	}
	public int startPrivateConvertation(ChatUser u1, ChatUser u2){
		if(!isLogin(u1.getId())) return 128;
		Conversation c = new PrivateChat(u1,u2,++COUNT.count);
		chats.put(COUNT.count, c);
		return 0;
	}
	public int startGroupConvertation(ChatUser ugen,List<ChatUser> u1){
		if(!isLogin(ugen.getId())) return 128;
		Conversation c = new GroupChat(u1,++COUNT.count);
		chats.put(COUNT.count, c);
		return 0;		
	}
	public int printMessage(ChatUser u, Integer i){
		int ret = preCheckConversation(u,i);
		if(ret!=1) return ret;
		Conversation c = chats.get(i);
		c.printMessage();
		return ret;
	}
 	public boolean isLogin(Integer i){
		return onlineUsers.containsKey(i);
	}
	public boolean hasUser(Integer i){
		return usersById.containsKey(i);
	}
	void printFriendList(ChatUser u){
		Set<Integer> set = contacts.get(u.getId());
		if(set==null) return;
		for(Integer i:set){
			System.out.println(usersById.get(i).getName());
		}
	}
	void addFriend(Request req){
		ChatUser receiver = req.getReceiver();
		receiver.receiveRequest(req);
	}
	void approve(Request req){
		ChatUser sender = req.getSender();
		ChatUser receiver = req.getReceiver();
		if(!contacts.containsKey(sender.getId())){
			contacts.put(sender.getId(), new HashSet<Integer>());
		}
		if(!contacts.containsKey(receiver.getId())){
			contacts.put(receiver.getId(), new HashSet<Integer>());
		}
		contacts.get(sender.getId()).add(receiver.getId());
		contacts.get(receiver.getId()).add(sender.getId());
	}
	void reject(Request req){
		return;
	}
//	public boolean addUser()
	@Override
	public int Login(Integer i) {
		// TODO Auto-generated method stub
		if(!usersById.containsKey(i)) return 404;
		else if(onlineUsers.containsKey(i)) return 10;
		ChatUser u = usersById.get(i);
		onlineUsers.put(i, u);
		u.setStatus(new UserStatusBusy());
		return 1;
	}
	@Override
	public int Logout(Integer i) {
		// TODO Auto-generated method stub
		if(!usersById.containsKey(i)) return 404;
		else if(!onlineUsers.containsKey(i)) return 10;
		ChatUser u = usersById.get(i);
		u.setStatus(new UserStatusOffline());
		onlineUsers.remove(i);
		return 1;
	}
}

interface LogOpr{
	public int Login(Integer i);
	public int Logout(Integer i);
}

class Request{
	private ChatUser sender,receiver;
	public Request(ChatUser sender,ChatUser receiver){
		this.sender = sender;
		this.receiver = receiver;
	}
	public ChatUser getSender(){return sender;}
	public ChatUser getReceiver(){return receiver;}
	
}

class ChatUser{

	private int userid;
	private String username;
	private UserStatus status = null;
	private ChatUserManager CUM;
	public void addFriend(ChatUser user){
		Request req = new Request(this,user);
		CUM.addFriend(req);
	}
	public void receiveRequest(Request req){
		if(req.getSender().getId()/2==0){
			approve(req);
		}
		else{
			reject(req);
		}
	}
	public void approve(Request req){
		CUM.approve(req);
	}
	public void reject(Request req){
		CUM.reject(req);
	}
	public String getName(){return username;}
	public int getId(){return userid;}
	public ChatUser(int userid, String username){
		this.userid = userid;
		this.username = username;
		this.status = new UserStatusOffline();
		CUM = ChatUserManager.getInstance();
	}
	public UserStatusType getStatus(){
		return status.getStatus();
	}
	public void setStatus(UserStatus st){
		status = st;
	}


	
}
class Message{
	private String content;
	private ChatUser speaker;
	private Date date;
	private String output;
	public Message(ChatUser u, String content){
		this.content = content;
		this.speaker = u;
		this.date = new Date();
		output = String.valueOf(date.getTime())+"  "+speaker.getName()+": "+content;
	}
	public void print(){
		System.out.println(output);
	}
}

abstract class Conversation{
	protected Set<ChatUser> participants;
	protected int id;
	protected List<Message> messages;
	public Conversation(int id){
		this.id = id;
		participants = new HashSet<>();
		messages = new ArrayList<>();
	}
	public Set<ChatUser> getUserList(){return participants;}
	public void printMessage(){
		for(Message m:messages) m.print();
	}
	public boolean addMessage(Message m){
		messages.add(m);
		return true;
	}
	public int getId(){ return id;}
}

class PrivateChat extends Conversation{
	public PrivateChat(ChatUser u1, ChatUser u2,int id){
		super(id);
		participants.add(u1);
		participants.add(u2);
	}
}
class GroupChat extends Conversation{
	public GroupChat(List<ChatUser> users, int id){
		super(id);
		if(users==null) return;
		for(ChatUser u:users) participants.add(u);
	}
}

abstract class UserStatus{
	private UserStatusType type;
	public UserStatus(UserStatusType tp){type =tp;}
	public UserStatusType getStatus(){return type;}	
}
class UserStatusOffline extends UserStatus{
	public UserStatusOffline(){
		super(UserStatusType.Offline);
	}
}
class UserStatusBusy extends UserStatus{
	public UserStatusBusy(){
		super(UserStatusType.Busy);
	}
}

enum UserStatusType{
	Offline,Away,Idle,Available,Busy
}
