

# Tick tracker

[![Plugin Installs](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/installs/plugin/ticktracker?style=for-the-badge&logo=appveyor&label=Active+installs)](https://runelite.net/plugin-hub/Tatters654)[![Plugin Rank](http://img.shields.io/endpoint?url=https://i.pluginhub.info/shields/rank/plugin/ticktracker?style=for-the-badge&logo=appveyor)](https://runelite.net/plugin-hub)

Tick Tracker tracks how well your selected game server is performing. It displays the data through a small, colored overlay at the top right corner of your screen. This is useful for determining how well the game server you are connected to is performing. The plugin is mostly used by the high level community, since the server performance is important for tick perfect gameplay.

The plugin works by tracking each server tick and calculating the time difference to the last observed server tick, and comparing the amount of time between ticks to the ideal time between ticks (600ms). The Oldschool Runescape servers aim to run at 600ms per tick, or 100 ticks per minute. This plugin is be accurate if you are on a stable connection with stable ping, no packet loss, jitter or other internet quality problems. 

The plugin currently does not detect or correct for being on a bad quality connection or Wi-Fi, so it is the users responsibility to comprehend the data.



![MinimapImage](TickTrackerMinimapImage.PNG)

The percentage value in the top right is a simple division as of version 1 is done using a formula of

<pre><code>Ticks within threshold / Total ticks passed</code></pre>

### Configuration
#### Height selector off-set

This controls the height of the small display. Useful if you use the RuneLite FPS plugin, since the default FPS plugin will drawn on top of the Tick Tracker default position

#### Color threshold

These options change the color of the small display in the top right. It is currently hardcoded into displaying:  
<span style="color:green">Green while above upper threshold  </span>.  
<span style="color:yellow">Yellow while below upper threshold, but above lower threshold</span>.  
<span style="color:red">Red while below lower threshold</span>.


#### Warn in chat about large tick lag  
Prints a red chat text message if the previous tick was over the user-configurable length threshold.

#### Disregard ticks on login

When you log in and the game is still loading in assets, rendering the world or receiving some initial startup data from the server, your perceived game ticks are very inconsistent. This option is to disregard X many ticks from login, to make the plugin more accurate. **I have left this option in the configuration for customizability reasons, but you probably should not change it if you do not know what you are doing.**

### TO DO
- Overhaul the code for the small overlay to allow more customization  
- Implement an option to use linear calculations instead of thresholds



