# Weather2 - Remastered
## Version 2.7.5-alpha, Optimization Test Update
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
