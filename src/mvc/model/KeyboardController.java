package mvc.model;

import mvc.model.extension.enums.Skip;

import java.util.Scanner;

public class KeyboardController {

    public static void main(String[] args){
        new KeyboardController().start();

    }

    private Scanner scan;
    private Model mp3Player;

    public KeyboardController(){
        this.scan = new Scanner(System.in);
        this.mp3Player = new Model();
    }

    public void start(){
        this.controller();
    }

    private void controller(){
        String input;
        String[] command;

        while (true){
            input = scan.nextLine();
            command = input.split(" ");

            switch (command[0]){

                case "isInCurrentPlaylist":
//                    this.mp3Player.addPlaylist("MP3_v2/playlists/mvc.model.playlist.Playlist.m3u");
                    String[] split = input.split("play ");
                    if(split.length > 1){
                        this.mp3Player.play(split[1]);
                    }
                    else{
                        this.mp3Player.play();
                    }
                    break;

                case "stop":
                    this.mp3Player.stop();
                    break;

                case "pause":
                    this.mp3Player.pause();
                    break;

                case "next":
                case "n":
                    this.mp3Player.skip(Skip.NEXT);
                    break;

                case "previous":
                case "prev":
                case "pr":
                    this.mp3Player.skip(Skip.PREVIOUS);
                    break;

                case "forward":
                case "forw":
                case "for":
                case "f":
                    this.mp3Player.skip(Skip.FORWARD);
                    break;

                case "backw":
                case "back":
                case "b":
                    this.mp3Player.skip(Skip.BACKWARD);
                    break;

                case "write":
                    this.mp3Player.writeTempPlistToSys();
                    break;

                case "add":
                    String[] splitS = input.split("add ");
                    String[] splitForSongs = splitS[1].split(", ");
                    String[] songs = new String[splitForSongs.length - 1];

                    for(int i = 1; i < splitForSongs.length; i++){
                        songs[i-1] = splitForSongs[i];
                    }

                    this.mp3Player.addSongs(splitForSongs);
                    break;

                default:
                    System.out.println("NOPE");

            }
        }

    }

}
