package dwg;

import java.io.File;

public class App {

    public static void main(String[] args) {
        var f = new File(args[0]);
        var d = new DWG(f);

        System.out.println("---- blocks ----");
        for (var i : d.blocks) {
            if (i != null) {
                System.out.println(i.name.s);
                for (var i2 : i.children()) {
                    if (i2 != null)
                        System.out.println(i2.type);
                }
            }
        }

        System.out.println("---- layers ----");
        for (var i : d.layers) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- textstyles ----");
        for (var i : d.textstyles) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- linetypes ----");
        for (var i : d.linetypes) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- views ----");
        for (var i : d.views) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- ucss ----");
        for (var i : d.ucss) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- vports ----");
        for (var i : d.vports) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- appids ----");
        for (var i : d.appids) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- dimstyles ----");
        for (var i : d.dimstyles) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- groups ----");
        for (var i : d.groups) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- mlinestyles ----");
        for (var i : d.mlinestyles) { if (i != null) System.out.println(i.name.s);}
        System.out.println("---- plotsettings ----");
        for (var i : d.plotsettings) { if (i != null) System.out.println(i.name.s);}

    }
}
