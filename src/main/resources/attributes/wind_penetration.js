// 风元素穿透
// 忽略目标一定百分比的风元素抗性

var priority = 75;
var combatPower = 1.2;
var attributeName = "wind_penetration";
var attributeType = "Attack";
var placeholder = "wind_penetration";
var pattern = "风元素穿透";
var patternSuffix = "%";
var element = "WIND";

// 注意: 元素穿透属性会在伤害计算时自动应用
// 通过 DamageBucket.applyResistances() 中检查攻击者的穿透值
// 公式: effectiveResistance = resistance * (1 - penetration / 100)
// 例如: 目标50点风抗性, 攻击者30%风穿透 -> 有效抗性 = 50 * (1 - 0.3) = 35
