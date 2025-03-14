/**
 */
package com.rockwellcollins.atc.agree.agree;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Enum Lit Expr</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link com.rockwellcollins.atc.agree.agree.EnumLitExpr#getEnumType <em>Enum Type</em>}</li>
 *   <li>{@link com.rockwellcollins.atc.agree.agree.EnumLitExpr#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see com.rockwellcollins.atc.agree.agree.AgreePackage#getEnumLitExpr()
 * @model
 * @generated
 */
public interface EnumLitExpr extends Expr
{
  /**
   * Returns the value of the '<em><b>Enum Type</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Enum Type</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Enum Type</em>' containment reference.
   * @see #setEnumType(DoubleDotRef)
   * @see com.rockwellcollins.atc.agree.agree.AgreePackage#getEnumLitExpr_EnumType()
   * @model containment="true"
   * @generated
   */
  DoubleDotRef getEnumType();

  /**
   * Sets the value of the '{@link com.rockwellcollins.atc.agree.agree.EnumLitExpr#getEnumType <em>Enum Type</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Enum Type</em>' containment reference.
   * @see #getEnumType()
   * @generated
   */
  void setEnumType(DoubleDotRef value);

  /**
   * Returns the value of the '<em><b>Value</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Value</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Value</em>' attribute.
   * @see #setValue(String)
   * @see com.rockwellcollins.atc.agree.agree.AgreePackage#getEnumLitExpr_Value()
   * @model
   * @generated
   */
  String getValue();

  /**
   * Sets the value of the '{@link com.rockwellcollins.atc.agree.agree.EnumLitExpr#getValue <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Value</em>' attribute.
   * @see #getValue()
   * @generated
   */
  void setValue(String value);

} // EnumLitExpr
