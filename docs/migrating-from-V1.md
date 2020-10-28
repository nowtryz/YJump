Migrating from V1
================

## Keeping YAML files

YJump `v2.0` is shipped with many storage solutions you can choose from :
 - [Nitrite DB](https://github.com/nitrite/nitrite-java) a NoSQL embedded database which the fastest and most reliable
  solution for the plugin at the moment (hence it's the default storage solution)
 - MySQL: the only remote database supported by the plugin
 - YAML files

If you want to keep V1's YAML files, follw the steps bellow:
 1. change implementation to `yaml`
    ```diff
    *** /YJump/config.yml
    @@ -62,68 +62,68 @@
      # Storages properties
      storage:
        # Implementation to use, either:
        #  - nitrite: A powerful NoSQL database using the H2's storage engine (fallback if not recognized)
        #  - yaml:    The ugly yaml files you all know
        #  - mysql:   Another ugly storage but well... it's a remote one
        #  - ... (more coming soon)
    -   implementation: "no2"
    +   implementation: "yaml"
    ```
 2. Delete `YJump/players/`, player scores cannot be imported
 3. Restart your server
 4. You should have a line like this one in the config:
    ```log
    The world of `%1$s` is not set or have changed, please update it with `/jump setworld name <world>`
    ```
 5. Fall distance is now editable for each jump and has been cahnged to `-1 (disabled)` for all of your old parkour. If
    you want to change this, ypu forst need to enter the admin panel of the desired parkours, to do this, use one of the
    following options:
       - Enter editor:
           1. use `/jump edit <jump name>` to enter the editor
           2. in the editor, use `/jump info` to show the admin panel
       - Use the *jumps* command
           1. use `/jumps <jump name>` to open the jump gui
           2. click on the `settings` icon to show the admin panel (the place, and the type of the icon depend on your
              gui configuration)
    Once you've done this, just click on the *bed* (or whatever icon you configured the gui to use) and edit the fall
    distance.
 6. Once you have followed these instructions, you will have your jumps ready? 
