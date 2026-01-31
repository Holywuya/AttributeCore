// 物理抗性
// 减少受到的物理伤害（包括普通攻击）

var priority = 45;
var combatPower = 1.2;
var attributeName = "physical_resistance";
var attributeType = "Other";
var placeholder = "physical_resistance";
var pattern = "物理抗性";
var patternSuffix = "";

// 注意: 抗性属性不需要实现 runDefense 函数
// 抗性会在 DamageBucket.applyResistances() 中自动应用
// 公式: damage * (1 - resistance / (resistance + 100))
// 例如: 50点物理抗性 -> 减伤 50/(50+100) = 33.3%
