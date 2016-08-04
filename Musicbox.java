package OOP;
import java.util.*;



// 1. can switch between among different MusicPlayer.
// 2. can play the playlist in different order sequence or random
// 3. can switch among different playlist or CD
// 4. can play a single music
public class Musicbox {
	private Player player;
	Map<String, PlayList> listcol;
	Map<String, CD> cdcol;
	public Musicbox(Player player,Set<PlayList> list, Set<CD> cd){
		this.player  = player;
		listcol = new HashMap<>();
		cdcol = new HashMap<>();
		for(PlayList l:list) listcol.put(l.getName(), l);
		for(CD c: cd) cdcol.put(c.getCDname(), c);
	}
	public void setListRandom(String name){
		PlayList cur = listcol.get(name);
		if(cur==null){
			System.out.println("can set "+name+" to random since no such list");
		}
		listcol.put(name, new RandomPlayList(name,cur.getList()));
		if(player.getPlayList().getName().equals(name)) player.setPlayList(listcol.get(name));
		
	}
	
	public void setListSequence(String name){
		PlayList cur = listcol.get(name);
		if(cur==null){
			System.out.println("can set "+name+" to random since no such list");
		}
		listcol.put(name, new SequencePlayList(name,cur.getList()));
		if(player.getPlayList().getName().equals(name)) player.setPlayList(listcol.get(name));
		
	}
	public void play(Song s){
		player.playSong(s);
	}
	public void play(){
		player.playSong();
	}
	public void next(){
		player.playNextSong();
	}
	public void setPlayer(Player p){
		player = p;
	}
	public void setList(String name){
		if(listcol.containsKey(name)){
			player.setPlayList(listcol.get(name));
		}
		else if(cdcol.containsKey(name)){
			player.setCD(cdcol.get(name));
		}
		else{
			System.out.println("No such playlist or CD,please check");
		}
		
	}
	public void addCD(CD c){cdcol.put(c.getCDname(), c);}
	public void addPlayList(PlayList l){listcol.put(l.getName(), l);}
	

}

class Song{
	private String name;
	private String artist;
	public Song(String name, String artist){
		this.name = name;
		this.artist=artist;
	}
	public String getName(){return name;}
	public String getArtist(){return artist;}
}

class CD{
	private String name;
	private List<Song> list;
	public CD(String name, List<Song> list){
		this.name = name;
		this.list = list;
	}
	public String getCDname(){return name;}
	public List<Song> getList(){return list;}
	
}

abstract class Player{
	private PlayList list;
	public Player(){this.list = null;}
	public PlayList getPlayList(){return list;}
	public void setPlayList(PlayList list){this.list = list;}
	public void setCD(CD cd){
		if(list==null) list = new RandomPlayList(cd.getCDname(),cd.getList());
		else list.genListfromCD(cd);
	}
	protected void beforeplaysong(){}
	protected void afterplaysong(){}
	public void playSong(){
		if(list == null) System.out.println("No list to play please check or assign a song");
		else{
			Song s= list.getSong();
			playSong(s);
		}
	}
	public void playSong(Song s){
		if(s==null){
			System.out.println("No song to play in current list, please check");
			return;
		}
		beforeplaysong();
		System.out.println("Playing---Song:"+s.getName());
		afterplaysong();
	}
	public void playNextSong(){
		Song s = list.getNextSong();
		playSong(s);
	}
}

class RockPlayer extends Player{	
	protected void beforeplaysong(){
		System.out.println("ROCKING BEGIN");
	}
	protected void afterplaysong(){
		System.out.println("ROCKING END");
	}
}

class BluePlayer extends Player{
	protected void beforeplaysong(){
		System.out.println("BLUE BEGIN");
	}
	protected void afterplaysong(){
		System.out.println("BLUE END");
	}	
}

interface playable{
	public Song getNextSong();
}

abstract class PlayList implements playable{
	private String name;
	private List<Song> list;
	protected int pt;
	public PlayList(String name, List<Song> list){
		this.name = name;
		this.list = list;
		pt = 0;
	}
	public List<Song> getList(){return list;}
	public String getName(){return name;}
	public void genListfromCD(CD cd){
		this.name = cd.getCDname();
		this.list = cd.getList();
		pt = 0;
	}
	public void addSong(Song s){list.add(s);}
	public int getLength(){return list.size();}
	protected abstract void setNextSongId();
	public Song getSong(){return list.get(pt);}
	public Song getNextSong(){
		if(list.size()==0){
			System.out.println(name+" is empty");
			return null;
		}
		setNextSongId();
		return getSong();

	}
	
}


class RandomPlayList extends PlayList{

	public RandomPlayList(String name, List<Song> list) {
		super(name, list);
	}

	protected void setNextSongId(){
		int l = getLength();
		Random rand = new Random();
		this.pt = rand.nextInt(l);
	}
}

class SequencePlayList extends PlayList{
	public SequencePlayList(String name, List<Song> list){
		super(name,list);
	}
	protected void setNextSongId(){
		this.pt = (this.pt+1) % getLength();
	}
}