# COMPSCI306-whac-a-mole

## why google map?

because it's funny;
because google map failed to display actual maps in previous assignments, leaving me with blank map;
because I cheesed my way out on sim2 by using Thread instead of AsyncTask and am doing it properly this time.

## features

4 difficulty levels with separate scoring rules, game length, game speed and BGM.

## goal

click the moles, avoid the mines.

## realization

google maps marker as mole holes, used repeatedly. marker tag records current status.
OnMarkerClickListener used to monitor and respond to user activity.
disabled rotation, zoom and scrolling of the map.
BGM played on a separate thread, adjusted according to game status and difficulty.
Game process run on AsyncTask that updates scoreboard and game status at the end of each turn.
