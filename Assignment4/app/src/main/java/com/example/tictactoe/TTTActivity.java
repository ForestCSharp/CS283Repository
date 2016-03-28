package com.example.tictactoe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TTTActivity extends ActionBarActivity {

    // TAG for logging
    private static final String TAG = "TTTActivity";

    // server to connect to
    protected static final int GROUPCAST_PORT = 20000;
    protected static final String GROUPCAST_SERVER = "ec2-54-165-202-12.compute-1.amazonaws.com";

    // networking
    Socket socket = null;
    BufferedReader in = null;
    PrintWriter out = null;
    boolean connected = false;

    // UI elements
    Button board[][] = new Button[3][3];
    Button bConnect = null;
    EditText etName = null;

    //MY Gameplay/Networking Data structures and Variables
    List<String> GroupNames;
    String MyGroup = new String();
    boolean bIsMyTurn;

    //Game State (0 is empty, 1 is this client, 2 is opponent)
    int BoardState[][] = new int[3][3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttt);

        // find UI elements defined in xml
        bConnect = (Button) this.findViewById(R.id.bConnect);
        etName = (EditText) this.findViewById(R.id.etName);
        board[0][0] = (Button) this.findViewById(R.id.b00);
        board[0][1] = (Button) this.findViewById(R.id.b01);
        board[0][2] = (Button) this.findViewById(R.id.b02);
        board[1][0] = (Button) this.findViewById(R.id.b10);
        board[1][1] = (Button) this.findViewById(R.id.b11);
        board[1][2] = (Button) this.findViewById(R.id.b12);
        board[2][0] = (Button) this.findViewById(R.id.b20);
        board[2][1] = (Button) this.findViewById(R.id.b21);
        board[2][2] = (Button) this.findViewById(R.id.b22);

        // hide login controls
        hideLoginControls();

        // make the board non-clickable
        disableBoardClick();

        // hide the board
        hideBoard();

        // assign OnClickListener to connect button
        bConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name = etName.getText().toString();
                // sanitity check: make sure that the name does not start with an @ character
                if (name == null || name.startsWith("@")) {
                    Toast.makeText(getApplicationContext(), "Invalid name",
                            Toast.LENGTH_SHORT).show();
                } else {
                    send("NAME,"+etName.getText());
                    ListGroups();
                }
            }
        });


        // assign a common OnClickListener to all board buttons
        View.OnClickListener boardClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int x=-1;
                int y= -1;

                if (bIsMyTurn) //Only Handle Button Input if it is your turn
                {

                    switch (v.getId()) {
                        case R.id.b00:
                            x = 0;
                            y = 0;
                            break;
                        case R.id.b01:
                            x = 0;
                            y = 1;
                            break;

                        case R.id.b02:
                            x= 0;
                            y=2;
                            break;

                        case R.id.b10:
                            x=1;
                            y=0;
                            break;

                        case R.id.b11:
                            x=1;
                            y=1;
                            break;

                        case R.id.b12:
                            x=1;
                            y=2;
                            break;

                        case R.id.b20:
                            x=2;
                            y=0;
                            break;

                        case R.id.b21:
                            x=2;
                            y=1;
                            break;

                        case R.id.b22:
                            x=2;
                            y=2;
                            break;

                        default:
                            break;
                    }

                    //Actually Play your turn
                    PlayTurn(true, x, y);
                }
            }
        };

        // assign OnClickListeners to board buttons
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                board[x][y].setOnClickListener(boardClickListener);


        // start the AsyncTask that connects to the server
        // and listens to whatever the server is sending to us
        connect();

    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy called");
        disconnect();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle menu click events
        if (item.getItemId() == R.id.exit) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ttt, menu);
        return true;
    }




    /***************************************************************************/
    /********* Networking ******************************************************/
    /***************************************************************************/

    /**
     * Connect to the server. This method is safe to call from the UI thread.
     */
    void connect() {

        new AsyncTask<Void, Void, String>() {

            String errorMsg = null;

            @Override
            protected String doInBackground(Void... args) {
                Log.i(TAG, "Connect task started");
                try {
                    connected = false;
                    socket = new Socket(GROUPCAST_SERVER, GROUPCAST_PORT);
                    Log.i(TAG, "Socket created");
                    in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream());

                    connected = true;
                    Log.i(TAG, "Input and output streams ready");

                } catch (UnknownHostException e1) {
                    errorMsg = e1.getMessage();
                } catch (IOException e1) {
                    errorMsg = e1.getMessage();
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException ignored) {
                    }
                }
                Log.i(TAG, "Connect task finished");
                return errorMsg;
            }

            @Override
            protected void onPostExecute(String errorMsg) {
                if (errorMsg == null) {
                    Toast.makeText(getApplicationContext(),
                            "Connected to server", Toast.LENGTH_SHORT).show();

                    hideConnectingText();
                    showLoginControls();

                    // start receiving
                    receive();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                    // can't connect: close the activity
                    finish();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Start receiving one-line messages over the TCP connection. Received lines are
     * handled in the onProgressUpdate method which runs on the UI thread.
     * This method is automatically called after a connection has been established.
     */

    void receive() {
        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... args) {
                Log.i(TAG, "Receive task started");
                try {
                    while (connected) {

                        String msg = in.readLine();

                        if (msg == null) { // other side closed the
                            // connection
                            break;
                        }
                        publishProgress(msg);
                    }

                } catch (UnknownHostException e1) {
                    Log.i(TAG, "UnknownHostException in receive task");
                } catch (IOException e1) {
                    Log.i(TAG, "IOException in receive task");
                } finally {
                    connected = false;
                    try {
                        if (out != null)
                            out.close();
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) {
                    }
                }
                Log.i(TAG, "Receive task finished");
                return null;
            }

            @Override
            protected void onProgressUpdate(String... lines) {
                // the message received from the server is
                // guaranteed to be not null
                String msg = lines[0];

                // TODO: act on messages received from the server
                if(msg.startsWith("+OK,NAME")) {
                    hideLoginControls();
                    showBoard();
                    return;
                }

                if(msg.startsWith("+ERROR,NAME")) {
                    Toast.makeText(getApplicationContext(), msg.substring("+ERROR,NAME,".length()), Toast.LENGTH_SHORT).show();
                    return;
                }

                //List of Groups returned
                if (msg.startsWith("+OK,LIST,GROUPS:"))
                {
                    //Process group names and store as a list of strings
                   // Toast.makeText(getApplicationContext(),
                        //    msg.substring("+OK,LIST,GROUPS:".length()),
                          //  Toast.LENGTH_SHORT).show();

                    //Parse Groups
                    String delims = "[,]+";
                    String[] tokens = msg.substring("+OK,LIST,GROUPS:".length()).split(delims);

                    GroupNames = Arrays.asList(tokens);

                    JoinGroup();


                    return;

                }

                //Handle Gameplay messages
                if (msg.startsWith("+MSG"))
                {
                    //Received message of Format "+MSG,<NAME>,<GROUP>,<BODY>"
                    //So we need element 3 of the tokens array
                    String delims = "[,]+";
                    String[] tokens = msg.split(delims);

                    //Parse body to get move
                    String body = tokens[3];
                    Toast.makeText(getApplicationContext(), body, Toast.LENGTH_SHORT).show();


                }


                // if we haven't returned yet, tell the user that we have an unhandled message
                Toast.makeText(getApplicationContext(), "Unhandled message: "+msg, Toast.LENGTH_SHORT).show();
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * Disconnect from the server
     */
    void disconnect() {
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    connected = false;
                }
                // make sure that we close the output, not the input
                if (out != null) {
                    out.print("BYE");
                    out.flush();
                    out.close();
                }
                // in some rare cases, out can be null, so we need to close the socket itself
                if (socket != null)
                    try { socket.close();} catch(IOException ignored) {}

                Log.i(TAG, "Disconnect task finished");
            }
        }.start();
    }

    /**
     * Send a one-line message to the server over the TCP connection. This
     * method is safe to call from the UI thread.
     *
     * @param msg
     *            The message to be sent.
     * @return true if sending was successful, false otherwise
     */
    boolean send(String msg) {
        if (!connected) {
            Log.i(TAG, "can't send: not connected");
            return false;
        }

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... msg) {
                Log.i(TAG, "sending: " + msg[0]);
                out.println(msg[0]);
                return out.checkError();
            }

            @Override
            protected void onPostExecute(Boolean error) {
                if (!error) {
                    Toast.makeText(getApplicationContext(),
                            "Message sent to server", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error sending message to server",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, msg);

        return true;
    }

    /***************************************************************************/
    /***** UI related methods **************************************************/
    /***************************************************************************/

    /**
     * Hide the "connecting to server" text
     */
    void hideConnectingText() {
        findViewById(R.id.tvConnecting).setVisibility(View.GONE);
    }

    /**
     * Show the "connecting to server" text
     */
    void showConnectingText() {
        findViewById(R.id.tvConnecting).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the login controls
     */
    void hideLoginControls() {
        findViewById(R.id.llLoginControls).setVisibility(View.GONE);
    }

    /**
     * Show the login controls
     */
    void showLoginControls() {
        findViewById(R.id.llLoginControls).setVisibility(View.VISIBLE);
    }

    /**
     * Hide the tictactoe board
     */
    void hideBoard() {
        findViewById(R.id.llBoard).setVisibility(View.GONE);
    }

    /**
     * Show the tictactoe board
     */
    void showBoard() {
        findViewById(R.id.llBoard).setVisibility(View.VISIBLE);
    }


    /**
     * Make the buttons of the tictactoe board clickable if they are not marked yet
     */
    void enableBoardClick() {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                if ("".equals(board[x][y].getText().toString()))
                    board[x][y].setEnabled(true);
    }

    /**
     * Make the tictactoe board non-clickable
     */
    void disableBoardClick() {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                board[x][y].setEnabled(false);
    }

    //My Methods

    //Searches for a group from the list of groups on the server
    //Preference for (1/2) groups, but will join/create a (0/2) group if necessary
    void ListGroups()
    {
        //Send request to list groups, which attempts to join
        send("LIST,GROUPS");


    }

    //Attempts to join (1/2) group, only creating a new group if (1/2) does not exist
    //(REMINDER: EMPTY GROUPS ARE DESTROYED BY GROUPCAST)
    void JoinGroup()
    {
        String half_full = "(1/2)";

        List<String> HalfFullGroups = new LinkedList<String>();

        //Process List of Groups, find (1/2) groups and (0/2) groups
        for (String Group : GroupNames)
        {
            Toast.makeText(getApplicationContext(), Group,Toast.LENGTH_SHORT).show();
            if (Group.contains(half_full))
            {
                HalfFullGroups.add(Group.replace(half_full, ""));
            }
        }

        //Try to join 1/2 group
        if (!HalfFullGroups.isEmpty())
        {
            Toast.makeText(getApplicationContext(), HalfFullGroups.get(0),Toast.LENGTH_SHORT).show();

            //Join first half-full group
            send("JOIN," + HalfFullGroups.get(0));
            MyGroup = HalfFullGroups.get(0);
            //Play game as second player (Play first move)
            bIsMyTurn = true;
        }
        else //Create and Join a new group
        {
            int GroupNum = GroupNames.size();

            //Create new group, with group number = size of Total number of groups
            send("JOIN,@group"+ GroupNum +",2");
            MyGroup = "@group"+ GroupNum;
            //Play game as first player (Wait on First move)
            bIsMyTurn = false;
        }

    }

    //Handles input to board, and checks for completion conditions,
    // then sends turn to opponent if bMyPlay
    void PlayTurn(boolean bMyPlay, int row, int col)
    {
        int boardVal = (bMyPlay) ? 1 : 2;

        if (BoardState[row][col] == 0)
        {
            BoardState[row][col] = boardVal;
        }
        else //Handle Invalid Input (This should only occur on sending Client side)
        {

        }

        //Check for win conditions (8 conditions to check)


        //Update our board w/ X and Send to Opponent if bMyPlay
        if (bMyPlay)
        {
            SetButton(1,row,col);
            SendPlay(row,col);
            bIsMyTurn = false;
        }
        else //Otherwise, update our board w/ O
        {
            SetButton(2,row,col);
            bIsMyTurn = true;
        }
    }

    //Sends play as MSG
    void SendPlay(int row, int col)
    {
        send("MSG," + MyGroup + "," + row + " " + col );
    }

    //Sets button with X, O, or blank
    void SetButton(int val, int row, int col)
    {
        Button button = (Button) findViewById(R.id.b00);

        if (row == 0)
        {
            if (col == 0)
            {
                button = (Button) findViewById(R.id.b00);
            }
            else if (col == 1)
            {
                button = (Button) findViewById(R.id.b01);
            }
            else if (col ==2)
            {
                button = (Button) findViewById(R.id.b02);
            }
        }
        else if (row == 1)
        {
            if (col == 0)
            {
                button = (Button) findViewById(R.id.b10);
            }
            else if (col == 1)
            {
                button = (Button) findViewById(R.id.b11);
            }
            else if (col ==2)
            {
                button = (Button) findViewById(R.id.b12);
            }
        }
        else if (row == 2)
        {
            if (col == 0)
            {
                button = (Button) findViewById(R.id.b20);
            }
            else if (col == 1)
            {
                button = (Button) findViewById(R.id.b21);
            }
            else if (col ==2)
            {
                button = (Button) findViewById(R.id.b22);
            }
        }

        if (val == 1)
        {
            button.setText("X");
        }
        else if (val == 2)
        {
            button.setText("O");
        }

    }

}
