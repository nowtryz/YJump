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
 5. Once you have followed these instructions, you will have your jumps ready? 
