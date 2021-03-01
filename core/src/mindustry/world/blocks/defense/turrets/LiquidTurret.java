package mindustry.world.blocks.defense.turrets;

import arc.graphics.g2d.*;
import arc.struct.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import mindustry.world.meta.values.*;

import static mindustry.Vars.*;

public class LiquidTurret extends Turret{
    public ObjectMap<Liquid, BulletType> ammoTypes = new ObjectMap<>();
    public @Load("@-liquid") TextureRegion liquidRegion;
    public @Load("@-top") TextureRegion topRegion;
    public boolean extinguish = true;

    public LiquidTurret(String name){
        super(name);
        acceptCoolant = false;
        hasLiquids = true;
        loopSound = Sounds.spray;
        shootSound = Sounds.none;
        outlinedIcon = 1;
    }

    /** Initializes accepted ammo map. Format: [liquid1, bullet1, liquid2, bullet2...] */
    public void ammo(Object... objects){
        ammoTypes = OrderedMap.of(objects);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.ammo, new AmmoListValue<>(ammoTypes));
    }

    @Override
    public void init(){
        consumes.add(new ConsumeLiquidFilter(i -> ammoTypes.containsKey(i), 1f){
            @Override
            public boolean valid(Building entity){
                return entity.liquids.total() > 0.001f;
            }

            @Override
            public void update(Building entity){

            }

            @Override
            public void display(Stats stats){

            }
        });

        super.init();
    }

    @Override
    public TextureRegion[] icons(){
        if(topRegion.found()) return new TextureRegion[]{baseRegion, region, topRegion};
        return super.icons();
    }

    public class LiquidTurretBuild extends TurretBuild{
        @Override
        public void draw(){
            super.draw();
            
            if(liquidRegion.found()){
                Drawf.liquid(liquidRegion, x + tr2.x, y + tr2.y, liquids.total() / liquidCapacity, liquids.current().color, rotation - 90);
            }
            if(topRegion.found()) Draw.rect(topRegion, x + tr2.x, y + tr2.y, rotation - 90);
        }

        @Override
        public boolean shouldActiveSound(){
            return wasShooting;
        }

        @Override
        public void updateTile(){
            unit.ammo(unit.type().ammoCapacity * liquids.currentAmount() / liquidCapacity);

            super.updateTile();
        }

        @Override
        protected void findTarget(){
            if(extinguish && liquids.current().canExtinguish()){
                int tr = (int)(range / tilesize);
                for(int x = -tr; x <= tr; x++){
                    for(int y = -tr; y <= tr; y++){
                        Tile other = world.tileWorld(x + tile.x, y + tile.y);
                        //do not extinguish fires on other team blocks
                        if(other != null && Fires.has(x + tile.x, y + tile.y) && (other.build == null || other.team() == team)){
                            target = Fires.get(x + tile.x, y + tile.y);
                            return;
                        }
                    }
                }
            }

            super.findTarget();
        }

        @Override
        protected void effects(){
            BulletType type = peekAmmo();

            type.shootEffect.at(x + tr.x, y + tr.y, rotation, liquids.current().color);
            type.smokeEffect.at(x + tr.x, y + tr.y, rotation, liquids.current().color);
            shootSound.at(tile);

            if(shootShake > 0){
                Effect.shake(shootShake, shootShake, tile.build);
            }

            recoil = recoilAmount;
        }

        @Override
        public BulletType useAmmo(){
            if(cheating()) return ammoTypes.get(liquids.current());
            BulletType type = ammoTypes.get(liquids.current());
            liquids.remove(liquids.current(), 1f / ammoMultiplier(type));
            return type;
        }

        @Override
        public BulletType peekAmmo(){
            return ammoTypes.get(liquids.current());
        }

        @Override
        public boolean hasAmmo(){
            return ammoTypes.get(liquids.current()) != null && liquids.total() >= 1f / ammoMultiplier(ammoTypes.get(liquids.current()));
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return ammoTypes.get(liquid) != null
                && (liquids.current() == liquid || (ammoTypes.containsKey(liquid)
                && (!ammoTypes.containsKey(liquids.current()) || liquids.get(liquids.current()) <= 1f / ammoMultiplier(ammoTypes.get(liquids.current())) + 0.001f)));
        }
    }
}
