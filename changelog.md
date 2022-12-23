# Weather2 - Remastered
## Version 2.8, Storms Awaken
## Special Thanks To Corosus and the entire weather2 community
------------------------------
### Additions
  - Added in fronts
  - Added in particle render distance. Particles are now able to render outside chunk render range. (Does not work with Optifine installed)
  - Added back in the ability for wind to move entities (Modifiable in new wind config)
  - Added storm names. Natural hurricanes will now have names on the radar
  - Added in max particles config to limit how many can spawn.
  - Added in distant downfall config, allowing you to adjust how much distant downfall particles can spawn.
  - Added in rain change multiplier config, allowing you to adjust how fast rain rates can change.
  - Added more sound configs in ConfigVolume
  - Added in 2 new tiers of radar blocks to craft
  - Added in 13 new crafting components and harder recipes for every block
  - Added in 3 new handheld sensors to help you storm chase
  - Added in 6 new block sensors and 1 new block for crafting
  - Added new particles for when wind is very high
  - Added in debris clouds for tornados
  - Added in new radar icons
  - Added in new /storm create flags that give you much more control over each storm.
  - Added in a few new storm types that can be spawned with /storm create. Random for example spawns in a storm that is randomly picked based on your configs 
  - Added new Temperature, Humidity, and Pressure variables to the world. They do not account for storms atm
  - Added in a size curve multiplier config, allowing you to adjust how drastic size changes on each stage. To get a near-realistic experience, set this to 1.7
------------------------------
### Changes
  - Complete overhaul of the wind system, allowing for smoother wind transitions
  - Greatly optimized block grabbing for all storms. In certain circumstances, this should improve preformance by 2x
  - Complete overhaul of the weather manager system. If you played with 2.6 or 2.7, those storms will transfer over to the new system.
  - Changed the sound system to be easier to manage
  - Changed around the api and removed redundant methods in the api. Feel free to recommend anything that I can add into the api
  - Changed how hurricanes look. They should be a little better now.
  - Hurricanes are now much bigger than normal storms.
  - The normal radar has been nerfed dramatically.
  - Tornado funnels now change color based on what they pick up (limited for now)
  - All radars now ping at a rate specified by each radar. Information will only update when the radar pings
  - Changed the sound of tornados to have a deeper pitch
  - Wind sounds change based on how fast the wind is blowing
  - Some radar icons were updated to look a bit better
  - Changes the early stages of hurricanes to start from Tropical Disturbance at Thunderstorm stage
  - Rain rates are now more dynamic. They are based on the closest storm's rain rates rather than just assuming all storms need max rain
  - Changed how hail spawns around players. This should help solve bigger storms creating less hail.
  - Tornado/Hurricane chances are now configured in two seperate lists, allowing for any kind of stage to spawn naturally. Order does matter here.
------------------------------
### Removed
  - Removed tile entity grab restrictions. It is still not recommended to add them to grab lists 
  - Removed old chances for tornados/cyclone configs
------------------------------
### Fixes
  - Fixed storm sounds stopping when particles are disabled
  - Fixed several crashes related to storm ticking and block grabbing
  - Fixed sandstorm command not working
  - Fixed create command not working sometimes
  - Fixed ghost storms (Hopefully?)
  - Fixed F0 not touching down/lifting correctly
  - Fixed rain storms spawning everywhere
------------------------------
### Known Bugs
  - Severe lag when reloading any block list in configs in big modpacks
  - Severe lag with sandstorms
  - Severe server lag with huge tornados
  - Temperature, Humidity, and Pressure is not taking into consideration nearby fronts/storms
  - EZ Gui is too big for bigger gui scales
  - Render Distance config does not work with Optifine installed
  - Wind angle sometimes changes weirdly
  - Rain sometimes doesn't change correctly.
  - Hail rates get lower the more players are on.
------------------------------
There is probably a lot I left out. More coming soon!

## Version 2.7.7-alpha, Bug Fix Update
## Special Thanks To Corosus and Fartsy
------------------------------
### Changes
  - Vanilla weather will now work in dimensions with Weather disabled in the dimension settings
------------------------------
### Fixes
  - Fixed vanilla rain not working in disabled dimensions (Effects must be enabled)
  - Fixed the weather machine not causing a storm to rain
  - Fixed the weather machine not properly executing a tornado or cyclone properly
  - Fixed the EZ Gui Dimension page buttons
  - Fixed the Weather Radar displaying storms past the 3x3 boundary
  - Temporarily fixed pressure plates and buttons crashing the game when being grabbed by the grab list. They will still crash if added back to the grab list
  - Fixed the Weather Machine throwing UUID errors
  - Somewhat fixed future crashes with future configuration changes. Might still crash if dimensions are added or removed
------------------------------
Glad I was able to fix a few bugs that came up. This update should be way more stable now. Enjoy!

## Version 2.7.6-alpha, Optimization Test Update
## Special Thanks To Corosus and Fartsy
------------------------------
### Additions
  - New EZ Gui
  - Added some new Api classes for modders. Use WeatherAPI for now.
  - Added new config file Simulation
  - Added new config file Volume
  - Added simulation mode toggle to ConfigSimulation. Do not enable as it does not work right now
  - Added a block replace list to ConfigGrab. This allows you to specify what blocks turn to what
  - Added radar range to ConfigMisc
  - Added the enhanced fujita scale under ConfigStorm
  - Added max storm damage size to ConfigStorm. This adjusts the max size a damage path can be. Increasing this value increases lag
  - Added violent storms to the mod. When a storm is violent, it has the ability to grow larger and stronger than normal. Adjustable in ConfigStorm
  - Added new spawning flags to /storm create command. isNatural will spawn the storm naturally, neverDissipate will prevent the storm from dying, isFirenado will spawn the storm as a firenado
  - Added config descriptions to 99% of configs in the advanced settings menu
------------------------------
### Changes
  - Changed how items and blocks are registered
  - Changed how config lists are parsed. Will now accept any amount of spaces and commas.
  - Moved and renamed various classes and methods
  - Moved all netcode into ""packets"" under network package
  - Moved various config options to new config files
  - Moved all config files around in the advanced settings menu
  - Renamed all variables. Please redo all of your configs
  - When debug_mode_radar is enabled, the storm it is tracking will now be highlighted bold-gold.
  - Radar debug information has been dramatically changed to be more user friendly, and shows more information
  - The Fujita Scale now works based on tornado size, and the Enhanced Fujita Scale works based on tornado wind speed
  - The max storm size config now also adjusts maximum funnel size possible
  - Changed the weather2 command to be named storm
  - The storm command now allows you to refresh the grab list, spawn storms at a specific location, and supports new flags for storms
  - Tornados and Cyclones no longer are capped at Stage 5. The new max is stage 2147483647. Normal storms cannot pass Stage 5
  - Tornados now grow in size more smoothly
  - Cyclones have been changed to look more like cyclones
  - Cyclones have a bigger damage path than a tornado by default
  - Cyclones now grab entities more weakly than tornados
  - All tornados except for the ef0 tornado have a bigger damage path
  - Storms now darken skies and rain much farther than normal
  - Tornado damage sounds can now be heard a little farther
  - Hail will now continue to fall when the storm progresses to a tornado
  - Storms now increase their spin more smoothly as it progresses
  - Villagers will now respond to any siren going off
------------------------------
### Removed
  - Removed config file Storm
------------------------------
### Fixes
  - Optimized block grabbing code
  - Optimized most ticking methods
  - Optimized storm objects
  - Optimized weather system objects
  - Fixed sandstorms spawning in cold biomes
------------------------------
Not everything was included in the changelog as I forgot most of those changes. I'll do better next time :)