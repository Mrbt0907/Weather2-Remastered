# Weather2 Remastered - Version 2.8.6-indev for 1.12.2
**Into The Storm**<br>
Special thanks to Corosus, Fartsy, Brawler, and Shibva
---
### Additions
  - Added sub command refresh scene to allow the ability to restart the scene enhancer
  - Added a brand new scene enhancer with much more environmental visuals
  - Added a brand new advanced gui with much more quality of life improvements and needed fixes
  - Added the advanced config gui to the mod options tab
  - Added a different error message for the storm command for when weather is disabled
  - Added a few new sounds for the scene enhancer (More incoming)

### Changes
  - Changed minecraft forge version requirement from 14.23.5.2846 to 14.23.5.2860
  - Changed overcast value to be dependent on rain
  - Changed fog to only change color if it's close to the player
  - Changed particle rendering code to account for positions when rendering particles
  - Storm revival now converts storms to hurricanes or tornados based on where the revival took place
  - Storm lightning rates is now randomized based on the original lightning_bolt_1_in_x config
  - Adjusted weight values and entity grabbing mechanics to give more danger to storms
  - Changed storm command to be accessible by all players
  - Chang enable debug radar to be a non-enforced client side option again
  - Completely revamped the rain system visually as it now fully obscures the player under heavy rain with shaders off
  - Optimized a few math functions to speed up overall performance of the mod

### Fixed
  - Fixed rain splashes occurring with no rain
  - Fixed fog colors not looking right in specific circumstances
  - Fixed water spout colors not working
  - Fixed scene enhancer failing to spawn rain for some users
  - Fixed entities not taking proper damage when being thrown into something or down to the ground
  - Fixed the cascading lag associated with grab lists
  - Fixed tornado particles rendering in front of rain sometimes
  - Fixed grass path being picked up instead of being replaced to dirt
  - Fixed normal renderer's normal clouds being too small
  - Fixed normal renderer's wall clouds being non-existent
  - Fixed normal renderer's height multiplier for taller storms
  - Fixed overcast value not updating with the nearest storm
  - Fixed rain and overcast not setting correctly with multiple storms
  - Fixed distant downfall spawning with snowstorms
  - Fixed wind affecting non survival players
  - Fixed lifespan speed not refreshing when a storm revives
  - Fixed particle_scale_mult creating massive particles on any value that is different from 1.0
  - Fixed storms not converting back to tornados upon storm revival if they are a tropical cyclone
  - Fixed lightning not occurring fast enough
  - Fixed sound stuttering when the player moves really fast
  - Fixed particles clumping up at the center of all storms
  - Fixed dimension settings not working at all
  - Fixed ghost storms (again)
  - Fixed storms not spawning on the client when dimension was turned back on
  - Fixed storms not syncing correctly because of null pointer exceptions
  - Fixed configex command not setting options successfully server side
  - Fixed fire particles causing a crash
  - Fixed foliage shaders not working with update 2.8.6
  - Fixed leaves not blowing in the wind with update 2.8.6
---
This update brings forth many new opportunities in terms of visual enhancements. I am glad that I finally fixed this long standing rendering bug, and now visual upgrades will be underway!

---