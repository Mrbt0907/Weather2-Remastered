# Weather2 - Remastered
## Version 2.8.5.7-alpha, Small Bug Fix Update
## Special Thanks To Corosus and Fartsy
------------------------------
### Additions
  - Added an entity blacklist to the Grab config
  - Added particle_scale_mult to the Particle config, which multiplies the particle size by this value
  - Added some scuffed information to the api
  - Added recipes to all handheld sensors
  - Added tornado lifespan configs to the Storm config
------------------------------
### Changes
  - Made front storms move 2x slower, and made cloud particles move slower
  - Made heavy downfall sheets spawn earlier and with less quantity
------------------------------
### Fixes
  - Fixed skies, fog, and particles going black when a storm is nearby
  - Fixed short grass and long grass not being destroyed by default
  - Added a few missing localizations
------------------------------
And this should fix most of the bugs! I hope you all enjoy!


# Weather2 - Remastered
## Version 2.8.5.6-alpha, The Great Remaster
## Special Thanks To Corosus and Fartsy
------------------------------
### Additions
- Added fronts to the game
- Added many new storm spawning flags to use with the /storm create command
- Added 6 new sensor blocks, 3 new handheld sensors, and two new radars
- Added various crafting items
- Players and entities now move with the wind of the world, just like weather1
- Added the weather1 rendering system to the game
- Added hurricane names
- Added in an extended render distance config, allowing you to see particles at a much farther distance with minimal cost. DOES NOT WORK WITH OPTIFINE INSTALLED
- Added a lot of new configs to play around with
- Hurricanes now have names
------------------------------
### Changes
- Changed the particle spawning code to now utilize the new particle spawning api
- Completely revamped storm textures and visuals to look much more realistic
- Tornado particles now change color based on what block the tornado is on
- Resized storm particles to better match the high setting in the ez gui
- A small percentage of storms now follow fronts
- Completely revamped wind system
- Storm chances have been redone, and now have to pass each check to reach tornados
- Tornado and hurricane chances is now fully configurable, allowing you to set any stage to spawn based on percentages
- Updated all recipes to be a bit more challenging
- The radar block was downgraded to a 1x1 screen and a low refresh rate
- All radars now ping storms, showing only storms where they were when the ping occurred
- Increased the speed limit of particles to work better with new wind system
- Changed config location to weather2remastered to avoid conflicts
- Hail can now occur on any storm based on a percent chance
- Changed various config defaults to work with the new front system
- Changed the names of the thunder stage and supercell stage to match real life names for hurricanes (Tropical storm, disturbance, etc.)
- Updated the sound system
- Updated a few radar icons
- Updated the information in debug radar mode
------------------------------
### Removed
- Removed hailing supercell stage
- Temporarily removed mr crayfish's vehicle mod compatibility
------------------------------
### Fixes
- Fixed ghost storms bug
- Fixed /storm create not creating the right storm and instantly killing storms
- Fixed /storm create sandstorm not creating a sandstorm
- Probably fixed f0 not playing the forming animation
- Fixed storm sounds stopping when nothing is rendering
- Fixed many various crashes
- Fixed EZGui causing a crash when dimensions are changed
- Fixed max_particles config not limiting particles correctly
- Optimized the entire mod a bit more
- Fixed new ez gui not fitting with max gui size
- Fixed some inconsistencies with certain environmental variables
- Un-opped players can no longer open the advanced config and edit the ez gui settings
- Fixed some major configs not being forced by serverside settings
- Fixed some missing localizations
------------------------------
This was a huge update with so many new features that I did not keep track of again. There are many fixes, changes, and additions that I did not cover here. Without further a do, I hope you enjoy this update! (Expect a lot of minor bugs)