# System-Scale Sensor Burst

### Adds an initially available campaign ability that allows the player to perform a “System-Scale Sensor Burst” after a number of prerequisites have been met in order to discover salvage-worthy objects in the current star system and temporarily have a view of all fleets in the star system.

The prerequisites are as follows:

	Fleet sensor strength of at least 1000 (Can be adjusted via LunaLib).
	Have enough volatiles in the cargo bay (see later for numerical calculations)
	Be in the scanning position of the current star system (see later for definition)

Skill effects are as follows:

	-Consume a certain amount of volatiles
	-Finds and marks salvageable objects across the star system
	-Temporarily obtains detailed information on the location and composition of all fleets in the star system (from about three-quarters of the way through the end of the scan).
	-Player fleets are highly visible and moving speed during skill activation.
	-Reduces the readiness of several ships in the fleet that provide sensor strength (see later for details).
	-Reports the **expected** number of special items (including AI cores, ship blueprints, fighter wing blueprints, weapon blueprints, Hull mod specs, colony items, etc.) carried by all salvageable objects in the star system at the completion of the scan, which may deviate slightly from the actual value (can be turned off in LunaLib)

*Unable to scan while in hyperspace
*Scanning is  possible when ships are low on readiness.
*Can be added mid safe.

Volatile Depletion

	Only affected by celestial bodies and some objects in the current star system. The amount of volatiles required for each corresponding celestial body/object present in the star system is as follows:
	Black holes: 16
	Neutron stars: 11
	Supergiant: 9.5
	Giant stars: 8
	White dwarfs: 8
	Yellow/orange dwarfs: 6.5
	Red/Brown Dwarfs: 6
	Gas/ice giants: 4
	Solid planets/moons: 2
	Accretion Disk: 1
	Stable position: 0.5
	Gate: 0.5
	Jump Points: 0.5
	Salvageable objects: 0.5
	
	*Can be adjusted via LunaLib

CR Losses

	Capital Ship: 5%
	Cruiser: 10%
	Destroyer: 15%
	Frigates: 20%
	
	*Only ships that have an effect on the overall sensor strength of the fleet will lose CR
	
	*Can be adjusted via LunaLib

Scanning Location

	Any position in a Nebula system or a star system with only a central object can be scanned. (There will be no additional display on the map at this point, and the skill will always be available if all other conditions are met)
	For other star system, the scanning position is near the fourth or fifth Lagrangian point of the highest (outermost) object in orbit around the central object.
	
	*The fourth and fifth Lagrangian points are the positions 60 degrees ahead and 60 degrees behind, respectively, in an object's orbit.


## Special thanks
	English localization by Carl Phisher* He missspelled my nick name...

## Update Log

To do list.
	Feedback that when Loading the save with new gravity wells scan location would move. (Tagged scan location entities so they are skipped in jump point generation.)

v1.2.2

	Bug Fixes.
		Fixed a bug where the scan report would still show rare item information after jumping to hyperspace before the scan was complete.

v 1.2.1.2

	Bug Fixes.
		Fixed bug where jumping to hyperspace before scanning was complete will cause CTD

v 1.2.1.1

	New:
		LunaLib is now a dependency.

v 1.2.1:

	What's new.
		Added visual when scanning reaches maximum strength and when scanning ends.(a green wave going out from fleet)

	Bug Fixes.
		Special item reports will now take player skill modifiers into account.

v 1.2.0.

	What's New.
		Added Momiji Salvage report
		Reports the **expected** number of special items (including AI cores, ship blueprints, fighter wing blueprints, weapon blueprints, ship insertion blueprints, colony industrial special items, etc.) on all salvageable objects in the star system at the completion of a scan.
		May deviate slightly from the actual value.
		Can be turned off in settings

	Bug fixes.
		Fixed some bugs when using skills in Developer Mode.


v 1.1.5.

    Bug fixes.
        Really fixed the bug where the sensor cross section was miscalculated >_<, sorry for the problem in previous versions

v 1.1.4.

    Fixed.
        Scanning positions now rotate normally with their corresponding objects.
        Reduced the display of scan positions on the map
        Scan position display on the map can now be switched on/off in config.json or LunaLib.

    Bug fixes.
        Fixed the bug that modifying values via LunaLib did not work.
        Fixed some display bugs in skill descriptions
        Fixed a bug where the scan location entity would be regenerated every time the game was read, resulting in a deeper color on the map.

v 1.1.3.

    Modifications.
        Modified the texture of scanning location

    Bug fixes.
        Fixed the bug that jumping to hyperspace when the scanning intensity is maxed out will cause the sensor cross section to be displayed as 0.

v 1.1.2.

    Changes.
        Rendering of scan positions in the main menu has been (supposedly) completely removed.

v 1.1.1.

    Bug fix.
        Fixed a bug where the scan area did not follow the rotation of celestial bodies

v 1.1.0.

    New.
        Added a scanning location guide in the career map and Tab map that Kiruno can understand.

    Bug Fixes.
        Fixed a bug where the player would not have the Wide Sensor Scan skill when starting career mode with the tutorial episode.
        Now you will have the skill in all situations when the mod is enabled (even in archives from previous versions that started with the tutorial).