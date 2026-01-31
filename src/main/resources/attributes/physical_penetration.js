// 物理穿透
// 忽略目标一定百分比的物理抗性

var priority = 75;
var combatPower = 1.2;
var attributeName = "physical_penetration";
var attributeType = "Attack";
var placeholder = "physical_penetration";
var pattern = "物理穿透";
var patternSuffix = "%";
var element = "PHYSICAL";

// 注意: 物理穿透属性会在伤害计算时自动应用
// 通过 DamageBucket.applyResistances() 中检查攻击者的穿透值
// 公式: effectiveResistance = resistance * (1 - penetration / 100)
// 例如: 目标50点物理抗性, 攻击者30%物理穿透 -> 有效抗性 = 50 * (1 - 0.3) = 35
