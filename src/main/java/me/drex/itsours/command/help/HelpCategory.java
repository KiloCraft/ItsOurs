package me.drex.itsours.command.help;

public enum HelpCategory {

    GET_STARTED("getStarted", HelpPage.BASICS, HelpPage.SETTINGS, HelpPage.PERMISSIONS, HelpPage.RESIZING, HelpPage.SUBZONES),
    COMMAND("commands", HelpPage.COMMAND_1, HelpPage.COMMAND_2, HelpPage.COMMAND_3, HelpPage.COMMAND_4, HelpPage.COMMAND_5);

    private final String id;
    private final HelpPage[] pages;

    HelpCategory(String id, HelpPage... pages) {
        this.id = id;
        this.pages = pages;
    }

    public static HelpCategory getByID(String id) {
        for (HelpCategory value : values()) {
            if (value.id.equals(id)) return value;
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getCommand() {
        return "/claim help " + this.id + " %s";
    }

    public HelpPage[] getPages() {
        return pages;
    }

    public enum HelpPage {

        BASICS("How to Claim (Basics)", "<yellow>Type <gold><click:run_command:/claim select>/claim select</click> <yellow>then left click on a block to set the <gold>first <yellow>corner of your claim", "<yellow>Right click to set the other corner", "<yellow>Type <gold><click:suggest_command:/claim create >/claim create <name></click> <yellow>to create your claim!", "<yellow>To trust a player in your claim type <gold><click:suggest_command:/claim trust >/claim trust <claim> <player></click>", "<yellow>To untrust a player in your claim type <gold><click:suggest_command:/claim distrust >/claim distrust <claim> <player></click>"),
        SETTINGS("How to Claim (Settings)", "<light_purple>Settings allow you to change some properties of your claim, they are basically global permissions", "<yellow> To change a <gold>setting<yellow>, type <gold><click:suggest_command:/claim setting MyClaim set pvp true>/claim setting <claim> set <setting> [<green>true <gold>| <gray>unset <gold>| <red>false]</click>", "<yellow>To check a setting, type <gold><click:suggest_command:/claim setting MyClaim check pvp>/claim setting <claim> check <setting> </click>", ""),
        PERMISSIONS("How to Claim (Permissions)", "<light_purple>Permissions allow you to change permissions for each player alone", "<yellow> To change a <gold>permission<yellow>, type <gold><click:suggest_command:/claim permission MyClaim set DrexHD place true>/claim permission <claim> set <player> <permission> [<green>true <gold>| <gray>unset <gold>| <red>false]</click>", "<yellow>To check a permission, type <gold><click:suggest_command:/claim permission MyClaim check DrexHD place>/claim setting <claim> check <player> <permission> </click>", ""),
        RESIZING("How to Claim (Resizing)", "<light_purple>You can always change the size of your claim if you aren't happy with it!", "<yellow>To <gold>expand<yellow> your claim in a direction, type <gold><click:suggest_command:/claim expand 5 >/claim expand <distance></click>", "<yellow>To <gold>shrink <yellow>a claim you do the same thing but replace \"<gold>expand<yellow>\" with \"<gold>shrink<yellow>\""),
        SUBZONES("How to Claim (Subzones)", "<light_purple>Subzones allow you to have separate permissions / settings in certain areas of your claim", "<yellow>To create a <gold>subzone<yellow>, you select an area inside your claim the way you would select a normal claim", "<yellow>Type <gold><click:suggest_command:/claim create >/claim create <name></click> <yellow>to create your subzone!"),
        COMMAND_1("Claim commands", "<gold>blocks <yellow>Shows your amount of claim blocks", "<gold>create <yellow>Creates a claim", "<gold>distrust <yellow>Removes trusted role from a player", "<gold>expand <yellow>Expands your claim in the direction your looking"),
        COMMAND_2("Claim commands", "<gold>fly <yellow>Allows you to fly in claims (if you have permission)", "<gold>help <yellow>Shows helpful information about claiming", "<gold>hide <yellow>Hides the borders of your claim", "<gold>info <yellow>Shows basic information about a claim"),
        COMMAND_3("Claim commands", "<gold>list <yellow>Lists all of your claims", "<gold>permissions <yellow>Allows you to modify / check player permissions", "<gold>remove <yellow>Removes your claim", "<gold>rename <yellow>Renames your claim"),
        COMMAND_4("Claim commands", "<gold>role <yellow>Allows you to modify / check player roles", "<gold>select <yellow>Toggles claim select mode (allows you to select claim without shovel)", "<gold>setting <yellow>Allows you to modify / check claim settings", "<gold>show <yellow>Shows the borders of your claim"),
        COMMAND_5("Claim commands", "<gold>shrink <yellow>Shrinks your claim in the direction your looking", "<gold>trust <yellow>Adds trusted role to a player", "<gold>trusted <yellow>Shows a list of trusted players");

        private final String title;
        private final String[] lines;

        HelpPage(String title, String... lines) {
            this.title = title;
            this.lines = lines;
        }

        public String getTitle() {
            return title;
        }

        public String[] getLines() {
            return lines;
        }
    }

}
