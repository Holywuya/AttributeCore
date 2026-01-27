// 导入 Bukkit 类
var Particle = Java.type("org.bukkit.Particle");

function onAttack(data, value, tags) {
    var attacker = data.attacker;
    var damage = data.getFinalDamage();

    // 简单的吸血逻辑
    if (Math.random() < 0.3) {
        var heal = damage * 0.1;
        var current = attacker.getHealth();
        // 设置血量 (假设最大20)
        attacker.setHealth(Math.min(20.0, current + heal));

        attacker.sendMessage("§a触发吸血: " + heal.toFixed(1));
        // 播放粒子
        attacker.getWorld().spawnParticle(Particle.HEART, attacker.getLocation(), 5);
    }
}